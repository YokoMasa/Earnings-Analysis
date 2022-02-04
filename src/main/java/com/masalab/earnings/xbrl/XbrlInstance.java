package com.masalab.earnings.xbrl;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.masalab.earnings.exception.AppException;
import com.masalab.earnings.xbrl.calc.CalculatedFact;
import com.masalab.earnings.xbrl.calc.CalculatedFactTree;
import com.masalab.earnings.xbrl.calc.CalculationTree;
import com.masalab.earnings.xbrl.calc.Concept;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class XbrlInstance {

    private Pattern reportingDatePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private Logger logger = LoggerFactory.getLogger(XbrlInstance.class);

    private CalculationTree calculationTree;
    private Map<String, Context> contexts;
    private FactSet factSet;
    private String assumedSoiRole;

    public CalculationTree getCalculationTree() {
        return this.calculationTree;
    }

    public void setCalculationTree(CalculationTree calculationTree) {
        this.calculationTree = calculationTree;
    }

    public void setFactSet(FactSet factSet) {
        this.factSet = factSet;
    }

    public FactSet getFactSet() {
        return this.factSet;
    }

    public void setContexts(Map<String, Context> contexts) {
        this.contexts = contexts;
    }

    public Map<String, Context> getContexts() {
        return contexts;
    }

    /**
     * Get calculated fact of the reporting year.
     * 
     * @param name Name of the fact.
     * @param role Role. nullable.
     * @return
     */
    public CalculatedFact getCalculatedFactCurrent(String name, String role) throws AppException {
        return getCalculatedFact(name, role, getReportingDate());
    }

    public String getReportingDate() throws AppException {
        Fact f = factSet.getSingleFact(XbrlConstants.FACT_REPORTING_DATE, null);
        if (f == null) {
            throw new AppException("Could not find reporting date. Tag: " + XbrlConstants.FACT_REPORTING_DATE);
        }

        if (reportingDatePattern.matcher(f.value).matches()) {
            return f.value.replace("-", "");
        } else {
            throw new AppException("Reporting date is not in expected format (yyyy-MM-dd) but: " + f.value);
        }
    }

    /**
     * Get calculated fact using the given date.
     * 
     * @param name Name of the fact.
     * @param role Role. nullable.
     * @param date Date associated with the fact. yyyyMMdd
     * @return
     */
    public CalculatedFact getCalculatedFact(String name, String role, String date) {
        if (calculationTree.hasConceptInTree(name)) {
            CalculatedFactTree tree = calculationTree.calculate(name, factSet, role, date);
            return tree.getFact(name);
        } else {
            return null;
        }
    }

    public String assumeSOIRole() {
        if (StringUtils.hasText(assumedSoiRole)) {
            return assumedSoiRole;
        }

        for (String role: calculationTree.getRoles()) {
            for (Pattern p: XbrlConstants.SOI_ROLE_WITHOUT_COMPREHENSIVE_PATTERNS) {
                if (p.matcher(role.toUpperCase()).find()) {
                    logger.debug("Assumed soi role: " + role);
                    assumedSoiRole = role;
                    return role;
                }
            }
        }

        for (String role: calculationTree.getRoles()) {
            for (Pattern p: XbrlConstants.SOI_ROLE_PATTERNS) {
                if (p.matcher(role.toUpperCase()).find()) {
                    logger.debug("Assumed soi role: " + role);
                    assumedSoiRole = role;
                    return role;
                }
            }
        }
        
        logger.warn("Failed to assume SOI role. Role dump follows.");
        for (String role: calculationTree.getRoles()) {
            logger.warn(role);
        }
        return null;
    }

    public Concept getConcept(String name) {
        if (calculationTree.hasConceptInTree(name)) {
            return calculationTree.getConcept(name);
        } else {
            return null;
        }
    }

    public StandardFacts getStandardFacts() throws AppException {
        StandardFacts standardFacts = getStandardFactsFromFactSet();
        
        String assumedSoiRole = assumeSOIRole();
        if (!calculationTree.hasRootConcept(assumedSoiRole)) {
            logger.warn("No root concept found for role: " + assumedSoiRole);
            return standardFacts;
        }

        List<Concept> rootConcepts = getCalculationTree().getRootConcepts(assumedSoiRole);
        Concept soiConcept = null;
        for (Concept c: rootConcepts) {
            if (c.hasConceptInDescendants(XbrlConstants.FACT_REVENUE1) || c.hasConceptInDescendants(XbrlConstants.FACT_REVENUE2) || c.hasConceptInDescendants(XbrlConstants.FACT_REVENUE3)) {
                soiConcept = c;
            }
        }

        if (soiConcept == null) {
            logger.warn("Failed to find soi concept. Role: " + assumedSoiRole + ", Search keys: " + XbrlConstants.FACT_REVENUE1 + ", " + XbrlConstants.FACT_REVENUE2 + ", " + XbrlConstants.FACT_REVENUE3);
            return standardFacts;
        }

        standardFacts.soiRole = assumedSoiRole;
        CalculatedFactTree tree = calculationTree.calculate(soiConcept.getName(), factSet, assumedSoiRole, getReportingDate());
        standardFacts.rootFact = tree.getFact(soiConcept.getName());

        return standardFacts;
    }

    private StandardFacts getStandardFactsFromFactSet() throws AppException {
        StandardFacts standardFacts = new StandardFacts();
        String reportingDate = getReportingDate();
        if (factSet.hasFact(XbrlConstants.FACT_REVENUE1, reportingDate)) {
            standardFacts.revenue = factSet.getSingleFact(XbrlConstants.FACT_REVENUE1, reportingDate).getValueAsDoubleIgnoringError();
        } else if (factSet.hasFact(XbrlConstants.FACT_REVENUE2, reportingDate)) {
            standardFacts.revenue = factSet.getSingleFact(XbrlConstants.FACT_REVENUE2, reportingDate).getValueAsDoubleIgnoringError();
        } else if (factSet.hasFact(XbrlConstants.FACT_REVENUE3, reportingDate)) {
            standardFacts.revenue = factSet.getSingleFact(XbrlConstants.FACT_REVENUE3, reportingDate).getValueAsDoubleIgnoringError();
        }

        if (standardFacts.revenue == 0) {
            throw new AppException("Could not find revenue.");
        }

        // CostOfGoodsSold
        if (factSet.hasFact(XbrlConstants.FACT_COST_OF_GOODS_SOLD, reportingDate)) {
            standardFacts.costOfRevenue = factSet.getSingleFact(XbrlConstants.FACT_COST_OF_GOODS_SOLD, reportingDate).getValueAsDoubleIgnoringError();
        } else if (factSet.hasFact(XbrlConstants.FACT_COST_OF_REVENUE, reportingDate)) {
            standardFacts.costOfRevenue = factSet.getSingleFact(XbrlConstants.FACT_COST_OF_REVENUE, reportingDate).getValueAsDoubleIgnoringError();
        }

        // GrossProfit
        if (factSet.hasFact(XbrlConstants.FACT_GROSS_PROFIT, reportingDate)) {
            standardFacts.grossProfit = factSet.getSingleFact(XbrlConstants.FACT_GROSS_PROFIT, reportingDate).getValueAsDoubleIgnoringError();
        } else if (standardFacts.costOfRevenue != 0) {
            standardFacts.grossProfit = standardFacts.revenue - standardFacts.costOfRevenue;
        }

        // OperatingIncomeLoss
        if (factSet.hasFact(XbrlConstants.FACT_OPERATING_INCOME_LOSS, reportingDate)) {
            standardFacts.operatingIncome = factSet.getSingleFact(XbrlConstants.FACT_OPERATING_INCOME_LOSS, reportingDate).getValueAsDoubleIgnoringError();
        }

        // OperatingExpenses
        if (factSet.hasFact(XbrlConstants.FACT_OPERATING_EXPENSES, reportingDate)) {
            standardFacts.operatingExpenses = factSet.getSingleFact(XbrlConstants.FACT_OPERATING_EXPENSES, reportingDate).getValueAsDoubleIgnoringError();
        } else if (factSet.hasFact(XbrlConstants.FACT_COSTS_AND_EXPENSES, reportingDate) && factSet.hasFact(XbrlConstants.FACT_COST_OF_REVENUE, reportingDate)) {
            standardFacts.operatingExpenses = factSet.getSingleFact(XbrlConstants.FACT_COSTS_AND_EXPENSES, reportingDate).getValueAsDoubleIgnoringError()
                - factSet.getSingleFact(XbrlConstants.FACT_COST_OF_REVENUE, reportingDate).getValueAsDoubleIgnoringError();
        }

        // PretaxIncome
        if (factSet.hasFact(XbrlConstants.FACT_PRETAX_INCOME1, reportingDate)) {
            standardFacts.pretaxIncome = factSet.getSingleFact(XbrlConstants.FACT_PRETAX_INCOME1, reportingDate).getValueAsDoubleIgnoringError();
        } else if (factSet.hasFact(XbrlConstants.FACT_PRETAX_INCOME2, reportingDate)) {
            standardFacts.pretaxIncome = factSet.getSingleFact(XbrlConstants.FACT_PRETAX_INCOME2, reportingDate).getValueAsDoubleIgnoringError();
        }

        return standardFacts;
    }

}
