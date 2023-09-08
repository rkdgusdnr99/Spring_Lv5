package com.example.postlv3.service;

import com.example.postlv3.dto.CommentRequestDto;
import com.example.postlv3.dto.CommentResponseDto;
import com.example.postlv3.dto.StatusResponseDto;
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


@Service// 빈으로 등록
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    private final UserRepository userRepository;

    private final PostRepository postRepository;



    // 댓글 작성
    public CommentResponseDto createComment(CommentRequestDto requestDto) {
        User currentUser = getCurrentUser();
        Post post = postRepository.findPostById(requestDto.getPostId());

        // entity 관계 설정 추가
        Comment comment = new Comment(requestDto, post);
        comment.setUser(currentUser);

        // db에 저장
        Comment saveComment = commentRepository.save(comment);

        // 반환
        CommentResponseDto responseDto = new CommentResponseDto(saveComment);
        return responseDto;

    }

    // 댓글 수정
    @Transactional
    public CommentResponseDto updateComment(Long id, CommentRequestDto requestDto) {
        User currentUser = getCurrentUser();
        Comment comment = findComment(id);

        if (validateUserAuthority(comment, currentUser)) {
            comment.update(requestDto);
            return new CommentResponseDto(comment);
        }
        else {
            return new CommentResponseDto("본인의 댓글만 수정 할 수 있습니다.", 400);
        }
    }

    // 댓글 삭제
    public StatusResponseDto deletePost(Long id) {
        User currentUser = getCurrentUser();
        Comment comment = findComment(id);

        if(validateUserAuthority(comment,currentUser)) {
            commentRepository.delete(comment);
            return new StatusResponseDto("삭제 성공", 200);
        }
        else
            return new StatusResponseDto("본인의 댓글만 삭제 할 수 있습니다.", 400);
    }

    // id 찾기
    public Comment findComment(Long id) {
        return commentRepository.findCommentById(id).orElseThrow(() ->
                new IllegalArgumentException("선택한 댓글은 존재하지 않습니다.")
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
    private boolean validateUserAuthority(Comment comment, User currentUser) {
        if (comment.getUser().equals(currentUser) || currentUser.getRole() == UserRoleEnum.ADMIN)
            return true;
        else
            return false;
    }
}

