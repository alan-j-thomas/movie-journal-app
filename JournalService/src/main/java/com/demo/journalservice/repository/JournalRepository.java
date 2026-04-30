package com.demo.journalservice.repository;


import com.demo.journalservice.entity.Journal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalRepository extends JpaRepository<Journal, Integer> {

    List<Journal> getJournalsByUserId(int userId);
}
