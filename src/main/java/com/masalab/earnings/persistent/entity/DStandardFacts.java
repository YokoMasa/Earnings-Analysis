package com.masalab.earnings.persistent.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "standard_facts")
public class DStandardFacts {

    @Id
    private String accessionNumber;
    private int year;
    private String cik;
    private String ticker;
    private double revenue;
    private double costOfRevenue;
    private double grossProfit;
    private double operatingExpenses;
    private double otherOperatingIncomeExpense;
    private double operatingIncome;
    private double pretaxIncome;
    private double epsBasic;
    private double epsDiluted;

    public String getAccessionNumber() {
        return this.accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public int getYear() {
        return this.year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getCik() {
        return this.cik;
    }

    public void setCik(String cik) {
        this.cik = cik;
    }

    public String getTicker() {
        return this.ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public double getRevenue() {
        return this.revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public double getCostOfRevenue() {
        return this.costOfRevenue;
    }

    public void setCostOfRevenue(double costOfRevenue) {
        this.costOfRevenue = costOfRevenue;
    }

    public double getGrossProfit() {
        return this.grossProfit;
    }

    public void setGrossProfit(double grossProfit) {
        this.grossProfit = grossProfit;
    }

    public double getOperatingExpenses() {
        return this.operatingExpenses;
    }

    public void setOperatingExpenses(double operatingExpenses) {
        this.operatingExpenses = operatingExpenses;
    }

    public double getOtherOperatingIncomeExpense() {
        return this.otherOperatingIncomeExpense;
    }

    public void setOtherOperatingIncomeExpense(double otherOperatingIncomeExpense) {
        this.otherOperatingIncomeExpense = otherOperatingIncomeExpense;
    }

    public double getOperatingIncome() {
        return this.operatingIncome;
    }

    public void setOperatingIncome(double operatingIncome) {
        this.operatingIncome = operatingIncome;
    }

    public double getPretaxIncome() {
        return this.pretaxIncome;
    }

    public void setPretaxIncome(double pretaxIncome) {
        this.pretaxIncome = pretaxIncome;
    }

    public double getEpsBasic() {
        return this.epsBasic;
    }

    public void setEpsBasic(double epsBasic) {
        this.epsBasic = epsBasic;
    }

    public double getEpsDiluted() {
        return this.epsDiluted;
    }

    public void setEpsDiluted(double epsDiluted) {
        this.epsDiluted = epsDiluted;
    }

}
