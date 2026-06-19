package com.footybox.diary;

import com.footybox.common.NotFoundException;
import com.footybox.football.FootballMatch;
import com.footybox.football.FootballMatchRepository;
import com.footybox.security.CurrentUserService;
import com.footybox.user.AppUser;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiaryService {

    private final CurrentUserService currentUserService;
    private final FootballMatchRepository matches;
    private final MatchLogRepository logs;
    private final FavoriteMatchRepository favorites;
    private final ReviewCommentRepository comments;

    public DiaryService(
            CurrentUserService currentUserService,
            FootballMatchRepository matches,
            MatchLogRepository logs,
            FavoriteMatchRepository favorites,
            ReviewCommentRepository comments
    ) {
        this.currentUserService = currentUserService;
        this.matches = matches;
        this.logs = logs;
        this.favorites = favorites;
        this.comments = comments;
    }

    @Transactional
    public MatchLogResponse logMatch(LogMatchRequest request) {
        AppUser user = currentUserService.currentUser();
        FootballMatch match = matches.findById(request.matchId())
                .orElseThrow(() -> new NotFoundException("Match not found."));

        MatchLog log = logs.findByUserAndMatch(user, match).orElseGet(MatchLog::new);
        log.setUser(user);
        log.setMatch(match);
        apply(log, request);
        return response(logs.save(log));
    }

    @Transactional
    public MatchLogResponse updateLog(Long logId, LogMatchRequest request) {
        AppUser user = currentUserService.currentUser();
        MatchLog log = logs.findByIdAndUser(logId, user)
                .orElseThrow(() -> new NotFoundException("Diary entry not found."));
        if (!log.getMatch().getId().equals(request.matchId())) {
            throw new IllegalArgumentException("A diary entry cannot be moved to another match.");
        }
        apply(log, request);
        return response(logs.save(log));
    }

    @Transactional
    public void deleteLog(Long logId) {
        AppUser user = currentUserService.currentUser();
        MatchLog log = logs.findByIdAndUser(logId, user)
                .orElseThrow(() -> new NotFoundException("Diary entry not found."));

        comments.deleteByMatchLog(log);

        logs.delete(log);
    }
    private void apply(MatchLog log, LogMatchRequest request) {
        log.setRating(request.rating());
        log.setReviewText(blankToNull(request.reviewText()));
        log.setSeenInStadium(Boolean.TRUE.equals(request.seenInStadium()));
        log.setContainsSpoilers(Boolean.TRUE.equals(request.containsSpoilers()));
        log.setPlayerOfMatchName(blankToNull(request.playerOfMatchName()));
        if (request.watchedAt() != null) {
            log.setWatchedAt(request.watchedAt());
        }

    }

    @Transactional(readOnly = true)
    public List<MatchLogResponse> myDiary() {
        AppUser user = currentUserService.currentUser();
        return logs.findTop25ByUserOrderByWatchedAtDesc(user).stream()
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MatchLogResponse> reviewsForMatch(Long matchId, boolean spoilerFree) {
        FootballMatch match = matches.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found."));
        return logs.findTop25ByMatchOrderByCreatedAtDesc(match).stream()
                .map(log -> response(log, spoilerFree))
                .toList();
    }

    @Transactional(readOnly = true)
    public MatchUserStateResponse matchState(Long matchId) {
        AppUser user = currentUserService.currentUser();
        FootballMatch match = matches.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found."));
        MatchLogResponse log = logs.findByUserAndMatch(user, match).map(this::response).orElse(null);
        return new MatchUserStateResponse(log, favorites.existsByUserAndMatch(user, match));
    }

    @Transactional(readOnly = true)
    public List<SavedMatchResponse> myFavorites() {
        AppUser user = currentUserService.currentUser();
        return favorites.findByUserOrderByCreatedAtDesc(user).stream().map(SavedMatchResponse::from).toList();
    }

    @Transactional
    public FavoriteResponse favorite(Long matchId) {
        AppUser user = currentUserService.currentUser();
        FootballMatch match = matches.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found."));
        if (!favorites.existsByUserAndMatch(user, match)) {
            FavoriteMatch favorite = new FavoriteMatch();
            favorite.setUser(user);
            favorite.setMatch(match);
            favorites.save(favorite);
        }
        return new FavoriteResponse(matchId, true);
    }

    @Transactional
    public FavoriteResponse unfavorite(Long matchId) {
        AppUser user = currentUserService.currentUser();
        FootballMatch match = matches.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found."));
        favorites.findByUserAndMatch(user, match).ifPresent(favorites::delete);
        return new FavoriteResponse(matchId, false);
    }

    @Transactional
    public CommentResponse comment(Long logId, CommentRequest request) {
        AppUser user = currentUserService.currentUser();
        MatchLog log = logs.findById(logId)
                .orElseThrow(() -> new NotFoundException("Review not found."));
        ReviewComment comment = new ReviewComment();
        comment.setUser(user);
        comment.setMatchLog(log);
        comment.setBody(request.body().trim());
        return CommentResponse.from(comments.save(comment));
    }

    private MatchLogResponse response(MatchLog log) {
        return response(log, false);
    }

    private MatchLogResponse response(MatchLog log, boolean spoilerFree) {
        List<CommentResponse> commentResponses = comments.findByMatchLogOrderByCreatedAtAsc(log).stream()
                .map(CommentResponse::from)
                .toList();
        return MatchLogResponse.from(log, commentResponses, spoilerFree);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
