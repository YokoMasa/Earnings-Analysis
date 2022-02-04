package com.masalab.earnings.xbrl;

import java.util.regex.Pattern;

public class XbrlConstants {

    public static final String NS_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    public static final String NS_XLINK = "http://www.w3.org/1999/xlink";
    public static final String NS_LINK = "http://www.xbrl.org/2003/linkbase";
    public static final String NS_INSTANCE = "http://www.xbrl.org/2003/instance";

    public static final String ROLE_CAL_LINKBASE_REF = "http://www.xbrl.org/2003/role/calculationLinkbaseRef";

    public static final String FACT_REPORTING_DATE = "DocumentPeriodEndDate";
    public static final String FACT_REVENUE1 = "Revenues";
    public static final String FACT_REVENUE2 = "RevenueFromContractWithCustomerExcludingAssessedTax";
    public static final String FACT_REVENUE3 = "RevenueFromContractWithCustomerIncludingAssessedTax";
    public static final String FACT_COST_OF_GOODS_SOLD = "CostOfGoodsAndServicesSold";
    public static final String FACT_COST_OF_REVENUE = "CostOfRevenue";
    public static final String FACT_COSTS_AND_EXPENSES = "CostsAndExpenses";
    public static final String FACT_GROSS_PROFIT = "GrossProfit";
    public static final String FACT_OPERATING_EXPENSES = "OperatingExpenses";
    public static final String FACT_OPERATING_INCOME_LOSS = "OperatingIncomeLoss";
    public static final String FACT_PRETAX_INCOME1 = "IncomeLossFromContinuingOperationsBeforeIncomeTaxesExtraordinaryItemsNoncontrollingInterest";
    public static final String FACT_PRETAX_INCOME2 = "IncomeLossFromContinuingOperationsBeforeIncomeTaxesMinorityInterestAndIncomeLossFromEquityMethodInvestments";

    public static final Pattern REPORTING_DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    public static final Pattern[] SOI_ROLE_WITHOUT_COMPREHENSIVE_PATTERNS = new Pattern[] {
        Pattern.compile("^(?!.*COMPREHENSIVE).*STATEMENTS-?OF-?OPERATIONS"),
        Pattern.compile("^(?!.*COMPREHENSIVE).*STATEMENT-?OF-?OPERATIONS"),
        Pattern.compile("^(?!.*COMPREHENSIVE).*STATEMENTS-?OF-?INCOME"),
        Pattern.compile("^(?!.*COMPREHENSIVE).*STATEMENT-?OF-?INCOME"),
        Pattern.compile("^(?!.*COMPREHENSIVE).*INCOME-?STATEMENTS"),
        Pattern.compile("^(?!.*COMPREHENSIVE).*INCOME-?STATEMENT"),
        Pattern.compile("^(?!.*COMPREHENSIVE).*STATEMENTS-?OF-?INCOMELOSS"),
        Pattern.compile("^(?!.*COMPREHENSIVE).*STATEMENT-?OF-?INCOMELOSS"),
        Pattern.compile("^(?!.*COMPREHENSIVE).*STATEMENTS-?OF-?EARNINGS"),
        Pattern.compile("^(?!.*COMPREHENSIVE).*STATEMENT-?OF-?EARNINGS"),
    };

    public static final Pattern[] SOI_ROLE_PATTERNS = new Pattern[] {
        Pattern.compile(".*STATEMENTS-?OF-?OPERATIONS"),
        Pattern.compile(".*STATEMENT-?OF-?OPERATIONS"),
        Pattern.compile(".*STATEMENTS-?OF-?INCOME"),
        Pattern.compile(".*INCOME-?STATEMENTS"),
        Pattern.compile(".*STATEMENT-?OF-?INCOME"),
        Pattern.compile(".*INCOME-?STATEMENT"),
        Pattern.compile(".*STATEMENTS-?OF-?INCOMELOSS"),
        Pattern.compile(".*STATEMENT-?OF-?INCOMELOSS"),
        Pattern.compile(".*STATEMENTS-?OF-?EARNINGS"),
        Pattern.compile(".*STATEMENT-?OF-?EARNINGS"),
    };

}
