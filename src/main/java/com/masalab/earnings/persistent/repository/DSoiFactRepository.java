package com.masalab.earnings.persistent.repository;

import com.masalab.earnings.persistent.entity.DSoiFact;
import com.masalab.earnings.persistent.entity.DSoiFactId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DSoiFactRepository extends JpaRepository<DSoiFact, DSoiFactId> {
    
}
