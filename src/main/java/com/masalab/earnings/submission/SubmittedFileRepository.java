package com.masalab.earnings.submission;

import java.util.List;

import com.masalab.earnings.exception.AppException;

public interface SubmittedFileRepository {
    public List<SubmittedFile> getSubmittedFiles(Submission submission) throws AppException;

    public SubmittedFile getXbrlInstance(Submission submission) throws AppException;
}
