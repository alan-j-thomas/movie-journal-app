package com.demo.userservice.external;

import com.demo.userservice.entity.Journal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "JOURNAL-SERVICE")
public interface JournalClient {

    @GetMapping("/journals/users/{userId}")
    List<Journal> getJournalByUser(@PathVariable int userId);
}
