package com.masalab.earnings.persistent.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;

import com.masalab.earnings.persistent.entity.DParseErrorLog;
import com.masalab.earnings.persistent.repository.DParseErrorLogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParseErrorLogServiceImpl implements ParseErrorLogService {

    @Autowired
    private DParseErrorLogRepository dParseErrorLogRepository;

    @Override
    public void logError(String accessionNumber, String cik, String ticker, int year, String message, Throwable t) {
        DParseErrorLog parseErrorLog = new DParseErrorLog();
        parseErrorLog.setAccessionNumber(accessionNumber);
        parseErrorLog.setCik(cik);
        parseErrorLog.setTicker(ticker);
        parseErrorLog.setYear(year);
        parseErrorLog.setLogTimestamp(new Timestamp(System.currentTimeMillis()));
        if (message != null && 200 < message.length()) {
            parseErrorLog.setContent(message.substring(0, 200));
        } else {
            parseErrorLog.setContent(message);
        }

        if (t != null) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            t.printStackTrace(writer);
            String stacktrace = stringWriter.toString();
            
            if (400 < stacktrace.length()) {
                stacktrace = stacktrace.substring(0, 400);
            }
            parseErrorLog.setStacktrace(stacktrace);
        }
        dParseErrorLogRepository.save(parseErrorLog);
    }
    
}
