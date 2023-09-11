package com.example.postlv3.aop;

import com.example.postlv3.entity.Comment;
import com.example.postlv3.entity.Post;
import com.example.postlv3.entity.UserRoleEnum;
import com.example.postlv3.exception.NotMineException;
import com.example.postlv3.repository.CommentRepository;
import com.example.postlv3.repository.PostRepository;
import com.example.postlv3.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j(topic = "IsItMineAop")
@Aspect
@Component
@RequiredArgsConstructor
public class IsItMineAop {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Pointcut("execution(* com.example.postlv3.controller.PostController.updatePost(..))")
    private void updatePost() {}
    @Pointcut("execution(* com.example.postlv3.controller.PostController.deletePost(..))")
    private void deletePost() {}
    @Pointcut("execution(* com.example.postlv3.controller.CommentController.updateComment(..))")
    private void updateComment() {}
    @Pointcut("execution(* com.example.postlv3.controller.CommentController.deleteComment(..))")
    private void deleteComment() {}

    @Before("updatePost() || deletePost()")
    public void checkPostMine(JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getUser().getId();
        UserRoleEnum currentUserRole = userDetails.getUser().getRole();

        if (!isPostMine(joinPoint, currentUserId) && currentUserRole != UserRoleEnum.ADMIN) {
            throw new NotMineException("당신의 게시글이 아닙니다.");
        }
    }

    private boolean isPostMine(JoinPoint joinPoint, Long userId) {
        // 실제로 자기 자신의 글인지 확인하는 로직을 작성합니다.
        // 주소에서 {id}를 추출하고, 해당 글의 작성자 ID와 현재 사용자의 ID를 비교하여 확인할 수 있습니다.

        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            Object arg = args[0];
            if (arg instanceof Long) {
                Long postId = (Long) arg;
                Post post = postRepository.findPostById(postId);
                return userId.equals(post.getUser().getId());
            }
        }
        return false;
    }

    @Before("updateComment() || deleteComment()")
    public void checkCommentMine(JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getUser().getId();
        UserRoleEnum currentUserRole = userDetails.getUser().getRole();

        if (!isCommentMine(joinPoint, currentUserId)&& currentUserRole != UserRoleEnum.ADMIN) {
            throw new NotMineException("당신의 댓글이 아닙니다.");
        }
    }

    private boolean isCommentMine(JoinPoint joinPoint, Long userId) {
        // 실제로 자기 자신의 글인지 확인하는 로직을 작성합니다.
        // 주소에서 {id}를 추출하고, 해당 글의 작성자 ID와 현재 사용자의 ID를 비교하여 확인할 수 있습니다.

        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            Object arg = args[0];
            if (arg instanceof Long) {
                Long commentId = (Long) arg;
                Comment comment = commentRepository.findCommentById(commentId);
                return userId.equals(comment.getUser().getId());
            }
        }
        return false;
    }
}
