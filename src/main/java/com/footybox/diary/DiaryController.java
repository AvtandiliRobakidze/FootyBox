package com.footybox.diary;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    @PostMapping("/diary/logs")
    MatchLogResponse logMatch(@Valid @RequestBody LogMatchRequest request) {
        return diaryService.logMatch(request);
    }

    @PutMapping("/diary/logs/{logId}")
    MatchLogResponse updateLog(@PathVariable Long logId, @Valid @RequestBody LogMatchRequest request) {
        return diaryService.updateLog(logId, request);
    }

    @DeleteMapping("/diary/logs/{logId}")
    ResponseEntity<Void> deleteLog(@PathVariable Long logId) {
        diaryService.deleteLog(logId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/diary/me")
    List<MatchLogResponse> myDiary() {
        return diaryService.myDiary();
    }

    @GetMapping("/matches/{matchId}/reviews")
    List<MatchLogResponse> reviewsForMatch(
            @PathVariable Long matchId,
            @RequestParam(defaultValue = "false") boolean spoilerFree
    ) {
        return diaryService.reviewsForMatch(matchId, spoilerFree);
    }

    @GetMapping("/matches/{matchId}/me")
    MatchUserStateResponse matchState(@PathVariable Long matchId) {
        return diaryService.matchState(matchId);
    }

    @GetMapping("/favorites/me")
    List<SavedMatchResponse> myFavorites() {
        return diaryService.myFavorites();
    }

    @PostMapping("/matches/{matchId}/favorite")
    FavoriteResponse favorite(@PathVariable Long matchId) {
        return diaryService.favorite(matchId);
    }

    @DeleteMapping("/matches/{matchId}/favorite")
    FavoriteResponse unfavorite(@PathVariable Long matchId) {
        return diaryService.unfavorite(matchId);
    }

    @PostMapping("/reviews/{logId}/comments")
    CommentResponse comment(@PathVariable Long logId, @Valid @RequestBody CommentRequest request) {
        return diaryService.comment(logId, request);
    }
}
