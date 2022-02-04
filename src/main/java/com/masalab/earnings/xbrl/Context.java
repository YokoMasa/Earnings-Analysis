package com.masalab.earnings.xbrl;

import java.util.Date;

public class Context {
    
    public String id;
    public PeriodType periodType;
    public Date instant;
    public Date startDate;
    public Date endDate;
    public String segmentContent;
    public String scenarioContent;
    
    public static enum PeriodType {
        INSTANT, RANGE
    }
}
