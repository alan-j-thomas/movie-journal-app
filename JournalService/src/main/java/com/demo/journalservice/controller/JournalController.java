package com.demo.journalservice.controller;

import com.demo.journalservice.dto.JournalRequest;
import com.demo.journalservice.entity.Journal;
import com.demo.journalservice.service.JournalServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@CrossOrigin("*")
@RestController
@RequestMapping("/journals")
@RequiredArgsConstructor
public class JournalController {

    
    private final JournalServiceImpl journalService;

    @PostMapping
    public ResponseEntity<Object> addJournal(@Valid @RequestBody Journal journal, BindingResult result){
    	
    	if (result.hasErrors()) {
		    FieldError fieldError = result.getFieldError();
		    String errorMsg = (fieldError != null) ? fieldError.getDefaultMessage() : "Validation error";
		    return ResponseEntity.badRequest().body(errorMsg);
		}

        return ResponseEntity.status(HttpStatus.CREATED).body(journalService.addJournal(journal));
    }

    @PostMapping("/add")
    public ResponseEntity<Object> addToJournal(@Valid @RequestBody JournalRequest request, BindingResult result){
    	
    	if (result.hasErrors()) {
		    FieldError fieldError = result.getFieldError();
		    String errorMsg = (fieldError != null) ? fieldError.getDefaultMessage() : "Validation error";
		    return ResponseEntity.badRequest().body(errorMsg);
		}

        return ResponseEntity.ok(journalService.addToJournal(request));
    }

    @GetMapping
    public ResponseEntity<List<Journal>> getAllJournals(){
        return ResponseEntity.ok(journalService.getAllJournals());
    }

    @GetMapping("/{journalId}")
    public ResponseEntity<Journal> getJournal(@PathVariable int journalId){
        return ResponseEntity.ok(journalService.getJournal(journalId));
    }

    @PutMapping("/{journalId}")
    public ResponseEntity<Journal> updateJournal(@PathVariable int journalId, @RequestBody JournalRequest journal){
        return ResponseEntity.ok(journalService.updateJournal(journalId, journal));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<Journal>> getJournalByUser(@PathVariable int userId) {
        return ResponseEntity.ok(journalService.getJournalsByUser(userId));
    }

    @DeleteMapping("/{journalId}")
    public ResponseEntity<String> deleteJournal(@PathVariable int journalId){
        return ResponseEntity.ok(journalService.deleteJournal(journalId));
    }
}
