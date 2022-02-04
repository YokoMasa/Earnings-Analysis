package com.masalab.earnings.persistent.entity;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@IdClass(DParseErrorLogId.class)
@Entity(name = "parse_error_log")
public class DParseErrorLog {
    
    @Id
    private String accessionNumber;
    @Id
    private Timestamp logTimestamp;
    private int year;
    private String cik;
    private String ticker;
    private String content;
    private String stacktrace;

    public String getStacktrace() {
        return this.stacktrace;
    }

    public void setStacktrace(String stacktrace) {
        this.stacktrace = stacktrace;
    }

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

    public Timestamp getLogTimestamp() {
        return this.logTimestamp;
    }

    public void setLogTimestamp(Timestamp logTimestamp) {
        this.logTimestamp = logTimestamp;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
