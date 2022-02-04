package com.masalab.earnings.persistent.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "submission")
public class DSubmission {

    public static final int NOT_IMPORTED = 0;
    public static final int IMPORTED = 1;
    public static final int ERROR_ON_IMPORT = 2;
    
    @Id
    private String accessionNumber;
    private int year;
    private String cik;
    private String ticker;
    private Date filingdate;
    private Date reportdate;
    private String form;
    private int importStatus;

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

    public Date getFilingdate() {
        return this.filingdate;
    }

    public void setFilingdate(Date filingdate) {
        this.filingdate = filingdate;
    }

    public Date getReportdate() {
        return this.reportdate;
    }

    public void setReportdate(Date reportdate) {
        this.reportdate = reportdate;
    }

    public String getForm() {
        return this.form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public int getImportStatus() {
        return this.importStatus;
    }

    public void setImportStatus(int importStatus) {
        this.importStatus = importStatus;
    }

}
