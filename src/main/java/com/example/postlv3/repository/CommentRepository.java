package com.example.postlv3.repository;

import com.example.postlv3.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Comment findCommentById(Long id);
    List<Comment> findAllByPostIdOrderByCreatedAtDesc(Long postId);

    Page<Comment> findAllByPostId(Long postId, Pageable pageable);
}
