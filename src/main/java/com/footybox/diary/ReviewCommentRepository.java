package com.footybox.diary;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {
    @EntityGraph(attributePaths = {"user"})
    List<ReviewComment> findByMatchLogOrderByCreatedAtAsc(MatchLog matchLog);

    void deleteByMatchLog(MatchLog matchLog);
}
