package com.masalab.earnings.persistent.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class DParseErrorLogId implements Serializable {
    
    private String accessionNumber;
    private Timestamp logTimestamp;

    public String getAccessionNumber() {
        return this.accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public Timestamp getLogTimestamp() {
        return this.logTimestamp;
    }

    public void setLogTimestamp(Timestamp logTimestamp) {
        this.logTimestamp = logTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DParseErrorLogId)) {
            return false;
        }
        DParseErrorLogId dParseErrorLogId = (DParseErrorLogId) o;
        return Objects.equals(accessionNumber, dParseErrorLogId.accessionNumber) && Objects.equals(logTimestamp, dParseErrorLogId.logTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessionNumber, logTimestamp);
    }

}
