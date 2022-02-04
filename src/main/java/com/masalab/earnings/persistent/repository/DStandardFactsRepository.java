package com.masalab.earnings.persistent.repository;

import com.masalab.earnings.persistent.entity.DStandardFacts;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DStandardFactsRepository extends JpaRepository<DStandardFacts, String> {
    
}
