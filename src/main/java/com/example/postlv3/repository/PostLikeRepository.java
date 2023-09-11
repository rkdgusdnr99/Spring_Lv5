package com.example.postlv3.repository;

import com.example.postlv3.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    PostLike findPostLikeByUserIdAndPostId(Long userId, Long postId);
}
