package com.demo.journalservice.service;



import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.demo.journalservice.dto.JournalRequest;
import com.demo.journalservice.entity.Journal;
import com.demo.journalservice.entity.Movie;
import com.demo.journalservice.exceptions.JournalNotFoundException;
import com.demo.journalservice.exceptions.MovieNotFoundException;
import com.demo.journalservice.external.MovieClient;
import com.demo.journalservice.repository.JournalRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JournalServiceImpl implements JournalService{

    
    private final JournalRepository journalRepository;

    private final MovieClient movieClient;

    private final RestTemplate restTemplate;
    
    private Logger logger = LoggerFactory.getLogger(JournalServiceImpl.class);

    @Override
    public Journal addJournal(Journal journal) {
        logger.info("Adding new journal: {}", journal);
        return journalRepository.save(journal);
    }

    @Override
    public Journal addToJournal(JournalRequest request) {
        logger.info("Adding to journal for userId: {}, movieTitle: {}", request.getUserId(), request.getMovieTitle());
        Movie movie = restTemplate.getForObject(
                "http://localhost:8082/movie/title/" + request.getMovieTitle(),
                Movie.class
        );

        if (movie == null || movie.getMovieId() == 0) {
            logger.warn("Movie not found for title: {}", request.getMovieTitle());
            throw new MovieNotFoundException();
        }

        Journal journal = new Journal();
        journal.setUserId(request.getUserId());
        journal.setMovieId(movie.getMovieId());
        journal.setMoodTag(request.getMoodTag());
        journal.setTitle(request.getTitle());
        journal.setContent(request.getContent());

        logger.info("Saving journal: {}", journal);
        return journalRepository.save(journal);
    }

    @Override
    public List<Journal> getAllJournals() {
        logger.info("Fetching all journals");
        List<Journal> allJournals = journalRepository.findAll();

        for (Journal journal : allJournals) {
            try {
                if (journal.getMovieId() > 0) {
                    journal.setMovies(List.of(movieClient.getMovies(journal.getMovieId())));
                } else {
                    journal.setMovies(List.of());
                }
            } catch (FeignException e) {
                logger.error("Error fetching movie for journal ID: {}, movieId: {} - {}", journal.getJournalId(), journal.getMovieId(), e.getMessage());
                journal.setMovies(List.of());
            }
        }

        return allJournals;
    }

    @Override
    public Journal getJournal(int journalId) {
        logger.info("Fetching journal with id: {}", journalId);
        
        Journal journal = journalRepository.findById(journalId).orElseThrow(() -> {
            logger.warn("Journal not found with id: {}", journalId);
            return new JournalNotFoundException();
        });
        
        journal.setMovies(List.of(movieClient.getMovies(journal.getMovieId())));
        return journal;
    }

    @Override
    public List<Journal> getJournalsByUser(int userId) {
        logger.info("Fetching journals for userId: {}", userId);
        List<Journal> journals = journalRepository.getJournalsByUserId(userId);

        return journals.stream().map(journal -> {
            try {
                Movie movie = movieClient.getMovies(journal.getMovieId());
                journal.setMovies(List.of(movie));
            } catch (FeignException e) {
                logger.error("Error fetching movie for journal ID: {}, movieId: {} - {}", journal.getJournalId(), journal.getMovieId(), e.getMessage());
                journal.setMovies(List.of());
            }
            return journal;
        }).toList();
    }

    @Override
    public Journal updateJournal(int journalId, JournalRequest journal) {
        Journal oldJournal = journalRepository.findById(journalId)
            .orElseThrow(() -> {
                logger.warn("Journal not found for update with id: {}", journalId);
                return new JournalNotFoundException();
            });

        oldJournal.setTitle(journal.getTitle());
        oldJournal.setContent(journal.getContent());
        oldJournal.setMoodTag(journal.getMoodTag());
        oldJournal.setUserId(journal.getUserId());

        Journal updated = journalRepository.save(oldJournal);
        oldJournal.setMovieId(updated.getMovieId());

        logger.info("Updated journal: {}", updated);
        return updated;
    }

    @Override
    public String deleteJournal(int journalId) {
        Journal oldJournal = journalRepository.findById(journalId).orElseThrow(() -> {
            logger.warn("Journal not found for delete with id: {}", journalId);
            return new JournalNotFoundException();
        });
        
        journalRepository.delete(oldJournal);
        
        logger.info("Journal deleted successfully with id: {}", journalId);
        return "Journal deleted Successfully!";
    }
    

   

}
