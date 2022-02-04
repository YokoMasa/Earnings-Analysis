package com.masalab.earnings.xbrl.calc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

public class CalculatedFact {

    private static final String ROLE_SOI = "http://fasb.org/us-gaap/role/statement/StatementOfIncome";
    
    private String name;
    private double absoluteVal;
    private Map<String, Double> calculatedValMap = new HashMap<>();
    private Map<String, List<CalculationItem<CalculatedFact>>> childrenMap = new HashMap<>();
    private String assumedMostValidRole;

    public String getName() {
        return this.name;
    }

    public double getAbsoluteVal() {
        return this.absoluteVal;
    }

    public double getCalculatedVal(String role) {
        return calculatedValMap.get(role);
    }

    public List<CalculationItem<CalculatedFact>> getChildren(String role) {
        return childrenMap.get(role);
    }

    public boolean hasChildren(String role) {
        return childrenMap.containsKey(role);
    }

    public double getCalculatedVal() {
        if (calculatedValMap.size() == 0) {
            return 0;
        }

        if (!StringUtils.hasText(assumedMostValidRole)) {

            // If custom cal defined, return that.
            for (String role: calculatedValMap.keySet()) {
                if (!role.contains("fasb.org")) {
                    assumedMostValidRole = role;
                    return calculatedValMap.get(assumedMostValidRole);
                }
            }

            double soiVal = 0;
            if (calculatedValMap.containsKey(ROLE_SOI)) {
                soiVal = calculatedValMap.get(ROLE_SOI);
            }

            double max = 0;
            String maxRole = "";
            for (String role: calculatedValMap.keySet()) {
                double absoluteVal = Math.abs(calculatedValMap.get(role));
                if (max < absoluteVal) {
                    maxRole = role;
                    max = absoluteVal; 
                } else if (!StringUtils.hasText(maxRole)) {
                    maxRole = role;
                }
            }

            if (max < soiVal) {
                assumedMostValidRole = ROLE_SOI;
            } else {
                assumedMostValidRole = maxRole;
            }
        }

        return calculatedValMap.get(assumedMostValidRole);
    }

    public void setCalculatedVal(String role, double calculatedVal, List<CalculationItem<CalculatedFact>> children) {
        this.calculatedValMap.put(role, calculatedVal);
        this.childrenMap.put(role, children);
    }

    public double getVal() {
        if (absoluteVal != 0) {
            return absoluteVal;
        } else {
            return getCalculatedVal();
        }
    }

    @Override
    public String toString() {
        return getTreeString(1);
    }

    public String getTreeString(int maxLevel) {
        return getTreeString(0, maxLevel);
    }

    private String getTreeString(int level, int maxLevel) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < level; i++) {
            indent.append("  ");
        }

        if (childrenMap.size() == 0 || maxLevel <= level) {
            return name + ": " + String.format("%.0f", getVal()) + "\n";
        }

        StringBuilder sb = new StringBuilder();

        sb.append(name).append(": ").append(String.format("%.0f", getVal())).append("\n");
        if (getCalculatedVal() == 0) {
            return sb.toString();
        }

        for (CalculationItem<CalculatedFact> calcItem: childrenMap.get(assumedMostValidRole)) {
            sb.append(indent).append(calcItem.getWeight() < 0 ? "(-) " : "(+) ").append(calcItem.getItem().getTreeString(level+1, maxLevel));
        }

        return sb.toString();
    }

    public String getAllTreeStrings() {
        StringBuilder sb = new StringBuilder();
        for (String role: childrenMap.keySet()) {
            List<CalculationItem<CalculatedFact>> calculatedFacts = childrenMap.get(role);
            sb.append(name).append(": ").append(calculatedValMap.get(role)).append("\n");
            for (CalculationItem<CalculatedFact> calcItem: calculatedFacts) {
                sb.append("   ")
                    .append(calcItem.getWeight() < 0 ? "(-) " : "(+) ")
                    .append(calcItem.getItem().getName()).append(": ")
                    .append(calcItem.getItem().getVal()).append("\n");
            }
        }
        return sb.toString();
    }

    public CalculatedFact(String name, double absoluteVal) {
        this.name = name;
        this.absoluteVal = absoluteVal;
    }

}
