package com.masalab.earnings.persistent.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@IdClass(DSoiFactId.class)
@Entity(name = "soi_fact")
public class DSoiFact {
    
    @Id
    private String accessionNumber;
    @Id
    private String name;
    private int year;
    private String cik;
    private String ticker;
    private double value;
    private int orderInSoi;
    private int levelInSoi;
    private int weight;

    public int getWeight() {
        return this.weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getAccessionNumber() {
        return this.accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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

    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getOrderInSoi() {
        return this.orderInSoi;
    }

    public void setOrderInSoi(int orderInSoi) {
        this.orderInSoi = orderInSoi;
    }

    public int getLevelInSoi() {
        return this.levelInSoi;
    }

    public void setLevelInSoi(int levelInSoi) {
        this.levelInSoi = levelInSoi;
    }

}
