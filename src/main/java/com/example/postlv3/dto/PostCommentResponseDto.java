package com.example.postlv3.dto;

import com.example.postlv3.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostCommentResponseDto {
    private Long id;
    private String title;
    private String contents;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private int likeCount;

    List<CommentResponseDto> comments;

    public PostCommentResponseDto(Post post, List<CommentResponseDto> comments) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.contents = post.getContents();
        this.username = post.getUser().getUsername();
        this.createdAt = post.getCreatedAt();
        this.modifiedAt = post.getModifiedAt();
        this.likeCount = post.getPostLikeList().size();
        this.comments = comments;
    }
}
