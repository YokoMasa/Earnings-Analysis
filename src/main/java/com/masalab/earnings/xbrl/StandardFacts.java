package com.masalab.earnings.xbrl;

import com.masalab.earnings.xbrl.calc.CalculatedFact;

public class StandardFacts {

    public double revenue; // Revenues
    public double costOfRevenue; // CostOfRevenue
    public double grossProfit; // GrossProfit
    public double operatingExpenses; // OperatingExpenses
    public double otherOperatingIncomeExpense; // OtherOperatingIncomeExpenseNet
    public double operatingIncome; // OperatingIncomeLoss
    public double pretaxIncome; // IncomeLossFromContinuingOperationsBeforeIncomeTaxesExtraordinaryItemsNoncontrollingInterest

    public String soiRole;
    public CalculatedFact rootFact;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n");
        sb.append("(+) Revenue:                            ").append(String.format("%1$ 15.0f", revenue)).append("\n");
        sb.append("(-)   Cost of revenue:                  ").append(String.format("%1$ 15.0f", costOfRevenue)).append("\n");
        sb.append("-----------------------------------------------------------").append("\n");
        sb.append("(+) Gross profit:                       ").append(String.format("%1$ 15.0f", grossProfit)).append("\n");
        sb.append("(-)   Operating expenses:               ").append(String.format("%1$ 15.0f", operatingExpenses)).append("\n");
        sb.append("(-)   Other operating income / expense: ").append(String.format("%1$ 15.0f", otherOperatingIncomeExpense)).append("\n");
        sb.append("-----------------------------------------------------------").append("\n");
        sb.append("(+) Operating income:                   ").append(String.format("%1$ 15.0f", operatingIncome)).append("\n");
        sb.append("-----------------------------------------------------------").append("\n");
        sb.append("(+) Pretax income:                      ").append(String.format("%1$ 15.0f", pretaxIncome));
        return sb.toString();
    }

}
