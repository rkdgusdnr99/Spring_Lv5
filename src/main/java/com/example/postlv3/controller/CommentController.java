package com.example.postlv3.controller;

import com.example.postlv3.dto.CommentRequestDto;
import com.example.postlv3.dto.CommentResponseDto;
import com.example.postlv3.dto.StatusResponseDto;
import com.example.postlv3.service.CommentService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 1. 댓글 작성
    @PostMapping("/comment")
    public CommentResponseDto createComment(@RequestBody CommentRequestDto requestDto) {
        return commentService.createComment(requestDto);
    }

    // 2. 댓글 수정
    @PutMapping("/comment/{id}")
    public CommentResponseDto updateComment(@PathVariable Long id, @RequestBody CommentRequestDto commentRequestDto) {
        return commentService.updateComment(id, commentRequestDto);
    }

    // 3. 댓글 삭제
    @DeleteMapping("/comment/{id}")
    public StatusResponseDto deleteComment(@PathVariable Long id) {
        return commentService.deleteComment(id);
    }

    // 4. 댓글 좋아요
    @PutMapping("/comment/{id}/like")
    public StatusResponseDto updateCommentLike(@PathVariable Long id) {
        return commentService.updateCommentLike(id);
    }


}
