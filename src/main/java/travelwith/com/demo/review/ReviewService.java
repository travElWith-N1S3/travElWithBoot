package travelwith.com.demo.review;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public ReviewVO reviewDetail(String tw_review_no) {
        return reviewRepository.findById(tw_review_no).orElse(null);
    }

    @Transactional
    public ReviewVO reviewUpdate(ReviewVO reviewVO) {
        return reviewRepository.save(reviewVO);
    }

    @Transactional
    public String reviewDelete(String tw_review_no) {
        reviewRepository.deleteById(tw_review_no);
        return "Deleted review with id: " + tw_review_no;
    }

    @Transactional
    public String reviewInsert(ReviewVO reviewVO) {
        reviewRepository.save(reviewVO);
        return "Inserted review with id: " + reviewVO.getTw_review_no();
    }

    public List<ReviewVO> reviewList() {
        return reviewRepository.findAll();
    }
}
