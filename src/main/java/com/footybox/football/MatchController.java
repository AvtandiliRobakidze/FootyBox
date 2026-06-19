package com.footybox.football;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final FootballArchiveService archiveService;

    public MatchController(FootballArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @GetMapping
    List<MatchSummaryResponse> matches(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "false") boolean spoilerFree,
            @RequestParam(required = false) String competitionCode,
            @RequestParam(required = false) Integer decade,
            @RequestParam(defaultValue = "newest") String sort
    ) {
        return archiveService.listMatches(query, spoilerFree, competitionCode, decade, sort);
    }

    @GetMapping("/{id}")
    MatchDetailResponse match(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean spoilerFree
    ) {
        return archiveService.match(id, spoilerFree);
    }
}
