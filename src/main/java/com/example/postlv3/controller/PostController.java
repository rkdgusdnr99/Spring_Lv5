package com.example.postlv3.controller;

import com.example.postlv3.dto.PostCommentResponseDto;
import com.example.postlv3.dto.RequestDto;
import com.example.postlv3.dto.ResponseDto;
import com.example.postlv3.dto.StatusResponseDto;
import com.example.postlv3.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 1. 게시글 작성
    @PostMapping("/post")
    public ResponseDto createPost(@RequestBody RequestDto requestDto) {
        return postService.createPost(requestDto);
    }

    // 2. 게시글 페이지 조회
    // 주소에서 받아오는 형식 ex) http://localhost:8080/api/posts?page=1&size=5&sortBy=Id&isAsc=true
    @GetMapping("/postPage")
    public Page<ResponseDto> getPage(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("isAsc") boolean isAsc
    ) {
        return postService.getPage(page-1, size, sortBy, isAsc);
    }

    // 3. 선택한 게시글의 댓글 페이지 조회
    // ex) http://localhost:8080/api/postPage/4?page=2&size=5&sortBy=Id&isAsc=false
    @GetMapping("/postPage/{id}")
    public PostCommentResponseDto getCommentPage(
            @PathVariable Long id,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("isAsc") boolean isAsc

    ) {
        return postService.getCommentPage(id, page-1, size, sortBy, isAsc);
    }

    // 4. 게시글 전체 조회
    @GetMapping("/posts")
    public List<PostCommentResponseDto> getPosts() {
        return postService.getPosts();
    }

    // 5. 선택한 게시글 조회
    @GetMapping("/post/{id}")
    public PostCommentResponseDto getPost(@PathVariable Long id) {
        return postService.getPost(id);
    }

    // 6. 게시글 수정
    @PutMapping("/post/{id}")
    public ResponseDto updatePost(@PathVariable Long id, @RequestBody RequestDto requestDto) {
        return postService.updatePost(id, requestDto);
    }

    // 7. 게시글 삭제
    @DeleteMapping("/post/{id}")
    public StatusResponseDto deletePost(@PathVariable Long id) {
        return postService.deletePost(id);
    }

    // 8. 게시글 좋아요
    @PutMapping("/post/{id}/like")
    public StatusResponseDto updatePostLike(@PathVariable Long id) {
        return postService.updatePostLike(id);
    }

}

