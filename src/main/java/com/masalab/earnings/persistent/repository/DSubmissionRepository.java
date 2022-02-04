package com.masalab.earnings.persistent.repository;

import com.masalab.earnings.persistent.entity.DSubmission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DSubmissionRepository extends JpaRepository<DSubmission, String> {

}
