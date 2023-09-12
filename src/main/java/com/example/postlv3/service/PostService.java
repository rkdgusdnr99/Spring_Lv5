package com.example.postlv3.service;

import com.example.postlv3.dto.*;
import com.example.postlv3.entity.*;
import com.example.postlv3.repository.CommentRepository;
import com.example.postlv3.repository.PostLikeRepository;
import com.example.postlv3.repository.PostRepository;
import com.example.postlv3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;


    // 게시글 작성
    public ResponseDto createPost(RequestDto requestDto) {
        User currentUser = getCurrentUser();

        // entity 관계 설정 추가
        Post post = new Post(requestDto);
        post.setUser(currentUser);

        // db에 저장
        Post savePost = postRepository.save(post);

        // 반환
        ResponseDto responseDto = new ResponseDto(savePost);
        return responseDto;
    }

    // 게시글 페이지 조회
    public Page<ResponseDto> getPage(int page, int size, String sortBy, boolean isAsc) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Post> postPage = postRepository.findAll(pageable);

        return postPage.map(ResponseDto::new);
        // 페이징된 게시글에 댓글까지 조회 기능, 실질적으로는 너무나 필요 없어 보여서 주석처리, 하지만 가능은 하다
//        return postsPage.map(post -> {
//            List<Comment> comments = post.getComments();
//            List<CommentResponseDto> commentResponseDtos = comments.stream()
//                    .map(CommentResponseDto::new)
//                    .collect(Collectors.toList());
//            return new PostCommentResponseDto(post, commentResponseDtos);
//        });
    }

    // 특정 게시글 페이지 조회 + 댓글 페이징
    public PostCommentResponseDto getCommentPage(Long id, int page, int size, String sortBy, boolean isAsc) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Post post = postRepository.findPostById(id);

        Page<Comment> commentPage = commentRepository.findAllByPostId(id, pageable);

        List<CommentResponseDto> commentResponseDtos = commentPage.getContent().stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());


        PostCommentResponseDto postCommentResponseDto = new PostCommentResponseDto(post, commentResponseDtos);

        return postCommentResponseDto;
    }

    // 전체 게시글 조회
    public List<PostCommentResponseDto> getPosts() {
        List<Post> posts = postRepository.findAllByOrderByModifiedAtDesc();
        List<PostCommentResponseDto> postCommentResponseDtos = new ArrayList<>();

        for (Post post : posts) {
            // comments are already fetched with the post due to @EntityGraph
            List<Comment> comments = post.getComments();
            List<CommentResponseDto> commentResponseDtos = new ArrayList<>();

            for (Comment comment : comments) {
                commentResponseDtos.add(new CommentResponseDto(comment));
            }

            PostCommentResponseDto postCommentResponseDto = new PostCommentResponseDto(post, commentResponseDtos);
            postCommentResponseDtos.add(postCommentResponseDto);
        }

        return postCommentResponseDtos;
    }

    // 선택한 게시글 조회
    public PostCommentResponseDto getPost(Long id) {
        Post post = postRepository.findPostById(id);

        List<Comment> comments = commentRepository.findAllByPostIdOrderByCreatedAtDesc(id);
        List<CommentResponseDto> commentResponseDtos = new ArrayList<>();

        for (Comment comment : comments) {
            commentResponseDtos.add(new CommentResponseDto(comment));
        }

        PostCommentResponseDto postCommentResponseDto = new PostCommentResponseDto(post, commentResponseDtos);

        return postCommentResponseDto;
    }

    // 게시글 수정
    @Transactional
    public ResponseDto updatePost(Long id, RequestDto requestDto) {
        Post post = findPost(id);
        post.update(requestDto);
        return new ResponseDto(post);
    }

    // 게시글 삭제
    public StatusResponseDto deletePost(Long id) {
        Post post = findPost(id);
        postRepository.delete(post);
        return new StatusResponseDto("삭제 성공", 200);
    }


    public StatusResponseDto updatePostLike(Long postId) {
        User currentUser = getCurrentUser();
        Long userId = currentUser.getId();
        PostLike postLike = postLikeRepository.findPostLikeByUserIdAndPostId(userId, postId);
        if (postLike == null) {
            Post currentPost = findPost(postId);
            PostLike postLikeAdd = new PostLike(currentPost, currentUser);
            postLikeRepository.save(postLikeAdd);
            return new StatusResponseDto("좋아요 + 1", 200);
        }
        else {
            postLikeRepository.delete(postLike);
            return new StatusResponseDto("좋아요 - 1", 200);
        }
    }

    // id 찾기
    public Post findPost(Long id) {
        return postRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("선택한 게시글은 존재하지 않습니다.")
        );
    }


    // 저장된 토큰과 현재 입력하는 유저네임 비교.
    // 직접 Controller에서 넣어주는 편이 좋다고 생각하지만, 안전을 위해 전달하지 않는게 좋다고 한다.
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String currentUsername = userDetails.getUsername();
            return userRepository.findByUsername(currentUsername).orElseThrow(
                    () -> new IllegalArgumentException("인증된 사용자를 찾을 수 없습니다.")
            );
        } else {
            throw new IllegalStateException("올바른 인증 정보가 아닙니다.");
        }
    }

}
