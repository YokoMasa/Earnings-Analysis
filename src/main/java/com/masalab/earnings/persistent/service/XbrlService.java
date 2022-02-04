package com.masalab.earnings.persistent.service;

import com.masalab.earnings.submission.Submission;
import com.masalab.earnings.xbrl.StandardFacts;

public interface XbrlService {
    
    public void saveFacts(StandardFacts standardFacts, Submission submission, String ticker);

    public void saveSubmission(Submission submission, String ticker);

    public void updateSubmissionStatus(String accessionNumber, int status);

    public boolean isImported(String accessionNumber);

}
