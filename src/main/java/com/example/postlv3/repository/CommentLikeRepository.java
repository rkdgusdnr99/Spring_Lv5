package com.example.postlv3.repository;

import com.example.postlv3.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    CommentLike findCommentLikeByUserIdAndCommentId(Long userId, Long commentId);
}
