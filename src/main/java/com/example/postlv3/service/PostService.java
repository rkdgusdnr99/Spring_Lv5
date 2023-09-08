package com.example.postlv3.service;

import com.example.postlv3.dto.*;
import com.example.postlv3.entity.Comment;
import com.example.postlv3.entity.Post;
import com.example.postlv3.entity.User;
import com.example.postlv3.entity.UserRoleEnum;
import com.example.postlv3.repository.CommentRepository;
import com.example.postlv3.repository.PostRepository;
import com.example.postlv3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;


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

    // 게시글 수정
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
    //
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
        User currentUser = getCurrentUser();
        Post post = findPost(id);

        if (validateUserAuthority(post,currentUser)) {
            post.update(requestDto);
            return new ResponseDto(post);
        }
        else
            return new ResponseDto("본인의 게시글만 수정 할 수 있습니다.", 400);
    }

    // 게시글 삭제
    public StatusResponseDto deletePost(Long id) {
        User currentUser = getCurrentUser();
        Post post = findPost(id);

        if (validateUserAuthority(post,currentUser)) {
            postRepository.delete(post);
            return new StatusResponseDto("삭제 성공", 200);
        }
        else {
            return new StatusResponseDto("본인의 게시글만 삭제 할 수 있습니다.", 400);
            //return ResponseEntity.status(200).body("상태코드 : " + HttpStatus.OK.value() + " 메세지 : 게시물 수정 성공");
        }
    }





    // id 찾기
    public Post findPost(Long id) {
        return postRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("선택한 게시글은 존재하지 않습니다.")
        );
    }


    // 저장된 토큰과 현재 입력하는 유저네임 비교
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

    // 수정,삭제 권한
    private boolean validateUserAuthority(Post post, User currentUser) {
        if (post.getUser().equals(currentUser) || currentUser.getRole() == UserRoleEnum.ADMIN)
            return true;
        else
            return false;
    }

}
