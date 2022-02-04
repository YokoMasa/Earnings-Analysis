package com.masalab.earnings.submission;

import java.util.List;

import com.masalab.earnings.exception.AppException;

public interface SubmissionRepository {

    public static final String FORM_10K = "10-K";
    public static final String FORM_10Q = "10-Q";

    public List<Submission> getSubmissions(String cik, String form) throws AppException;
    
}
