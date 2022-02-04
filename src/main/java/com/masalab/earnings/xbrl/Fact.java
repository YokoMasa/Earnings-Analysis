package com.masalab.earnings.xbrl;

public class Fact {
    public String name;
    public String ns;
    public String value;
    public String decimals;
    public String precision;
    public Context context;

    public double getValueAsDoubleIgnoringError() {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
