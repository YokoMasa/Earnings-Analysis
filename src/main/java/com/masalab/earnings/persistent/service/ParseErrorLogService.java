package com.masalab.earnings.persistent.service;

public interface ParseErrorLogService {
    
    public void logError(String accessionNumber, String cik, String ticker, int year, String message, Throwable t);

}
