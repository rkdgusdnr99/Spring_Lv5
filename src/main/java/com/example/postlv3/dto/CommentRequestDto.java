package com.example.postlv3.dto;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CommentRequestDto {
    private Long postId;
    private String contents;
}
