package travelwith.com.demo.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewVO, Long> {
    Page<ReviewVO> findByTwReviewTitleContainingOrTwReviewContentContaining(String title, String content, Pageable pageable);
}