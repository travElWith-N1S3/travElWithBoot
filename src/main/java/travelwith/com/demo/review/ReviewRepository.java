package travelwith.com.demo.review;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewVO, Long> {
    Page<ReviewVO> findByTwReviewTitleContainingOrTwReviewContentContainingOrderByTwReviewNoDesc(String title, String content, Pageable pageable);
    List<ReviewVO> findTop3ByOrderByTwReviewNoDesc();
}