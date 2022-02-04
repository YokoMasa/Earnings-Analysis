package com.masalab.earnings.persistent.entity;

import java.io.Serializable;
import java.util.Objects;

public class DSoiFactId implements Serializable {
    
    private String accessionNumber;
    private String name;

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

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DSoiFactId)) {
            return false;
        }
        DSoiFactId dSoiFactId = (DSoiFactId) o;
        return Objects.equals(accessionNumber, dSoiFactId.accessionNumber) && Objects.equals(name, dSoiFactId.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessionNumber, name);
    }


}
