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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;


    // 1. 게시글 작성
    // 제목,작성자명,비밀번호,작성 내용을 저장하고
    // 저장된 게시글을 client로 반환하기

    // lv2 => 작성자명(username)
    public ResponseDto createPost(RequestDto requestDto) {
        User currentUser = getCurrentUser();

        // entity 관계 설정 추가
        Post post = new Post(requestDto);
        post.setUser(currentUser);
        currentUser.getPosts().add(post);


        // db에 저장
        Post savePost = postRepository.save(post);

        // 반환
        ResponseDto responseDto = new ResponseDto(savePost);
        return responseDto;

    }

    // 2. 게시글 전체 조회
    // 전체 조회 : 제목,작성자명, 작성 내용, 작성 날짜
    // 작성 날짜 기준, 내림차순 정렬
//    public List<PostCommentResponseDto> getPosts() {
//        List<Post> posts = postRepository.findAllByOrderByModifiedAtDesc();
//        List<PostCommentResponseDto> postCommentResponseDtos = new ArrayList<>();
//
//        for (Post post : posts) {
//            List<Comment> comments = commentRepository.findAllByPostidOrderByCreatedAtDesc(post.getId());
//            List<CommentResponseDto> commentResponseDtos = new ArrayList<>();
//
//            for (Comment comment : comments) {
//                commentResponseDtos.add(new CommentResponseDto(comment));
//            }
//
//            PostCommentResponseDto postCommentResponseDto = new PostCommentResponseDto(post, commentResponseDtos);
//            postCommentResponseDtos.add(postCommentResponseDto);
//        }
//
//        return postCommentResponseDtos;
//    }


    // n+1 문제 수정 코드

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






    // 3. 선택한 게시글 조회
    // 선택한 게시글의 제목,작성자명,작성 날짜, 작성 내용을 조회하기
    // (검색 기능이 아닌, 간단한 게시글 조회)
    public PostCommentResponseDto getPost(Long id) {
        Post post = postRepository.findPostById(id);

        List<Comment> comments = commentRepository.findAllByPostidOrderByCreatedAtDesc(id);
        List<CommentResponseDto> commentResponseDtos = new ArrayList<>();

        for (Comment comment : comments) {
            commentResponseDtos.add(new CommentResponseDto(comment));
        }

        PostCommentResponseDto postCommentResponseDto = new PostCommentResponseDto(post, commentResponseDtos);

        return postCommentResponseDto;
    }

    // 4. 게시글 수정
    // 수정을 요청할 때 수정할 데이터와 비번을 같이 보내서 서버에서 일치 여부 확인 -> 트랜젝션 활용
    // 제목,작성자명,작성 내용 수정 후, 수정된 게시글 반환

    // 비밀번호 일치 -> 토큰 유효하면 수정 가능
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

    // 5. 게시글 삭제
    // 삭제를 요청할 때 비밀번호를 같이 보내서 비밀번호 일치 확인 한 후
    // 선택한 게시글 삭제 client로 성공했다는 표시 반환하기 ->

    // 수정과 동일하게 비밀번호 -> 유효 토큰
    public StatusResponseDto deletePost(Long id) {
        User currentUser = getCurrentUser();
        Post post = findPost(id);

        if (validateUserAuthority(post,currentUser)) {
            postRepository.delete(post);
            return new StatusResponseDto("삭제 성공", 200);
        }
        else {
            return new StatusResponseDto("본인의 게시글만 삭제 할 수 있습니다.", 400);
        }
    }

    // id 찾기
    public Post findPost(Long id) {
        return postRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("선택한 게시글은 존재하지 않습니다.")
        );
    }

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

    private boolean validateUserAuthority(Post post, User currentUser) {
        if (!(post.getUser().equals(currentUser) || currentUser.getRole() == UserRoleEnum.ADMIN))
            return false;
        else
            return true;
    }

}
