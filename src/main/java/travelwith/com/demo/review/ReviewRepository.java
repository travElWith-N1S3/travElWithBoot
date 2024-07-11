package travelwith.com.demo.review;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewVO, Long> {
    List<ReviewVO> findByTwReviewTitleContainingOrTwReviewContentContaining(String title, String content);
}