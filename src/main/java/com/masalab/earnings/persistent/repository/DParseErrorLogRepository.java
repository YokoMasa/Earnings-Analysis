package com.masalab.earnings.persistent.repository;

import com.masalab.earnings.persistent.entity.DParseErrorLog;
import com.masalab.earnings.persistent.entity.DParseErrorLogId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DParseErrorLogRepository extends JpaRepository<DParseErrorLog, DParseErrorLogId> {
    
}
