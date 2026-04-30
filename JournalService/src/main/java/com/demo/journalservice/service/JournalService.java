package com.demo.journalservice.service;

import com.demo.journalservice.dto.JournalRequest;
import com.demo.journalservice.entity.Journal;

import java.util.List;

public interface JournalService {

    Journal addJournal(Journal journal);
    Journal addToJournal(JournalRequest request);

    List<Journal> getAllJournals();
    Journal getJournal(int journalId);
    List<Journal> getJournalsByUser(int userId);
    Journal updateJournal(int journalId, JournalRequest journal);
    String deleteJournal(int journalId);
}
