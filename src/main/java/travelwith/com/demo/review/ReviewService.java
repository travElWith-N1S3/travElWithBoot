package travelwith.com.demo.review;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import travelwith.com.demo.image.ImageService;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ImageService imageService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public ReviewVO reviewDetail(Long twReviewNo) {
        return reviewRepository.findById(twReviewNo).orElse(null);
    }

    @Transactional
    public void reviewUpdate(ReviewVO reviewVO) {
        ReviewVO existingReview = reviewRepository.findById(reviewVO.getTwReviewNo()).orElse(null);
        if (existingReview != null) {
            existingReview.setTwReviewTitle(reviewVO.getTwReviewTitle());
            existingReview.setTwReviewContent(reviewVO.getTwReviewContent());
            existingReview.setTwReviewRating(reviewVO.getTwReviewRating());
            reviewRepository.save(existingReview);
        }
    }

    @Transactional
    public String reviewDelete(Long twReviewNo) {
        // 리뷰 삭제 전 이미지 삭제 처리
        try {
            // 리뷰 내용을 조회하여 이미지 URL을 추출하여 삭제
            ReviewVO review = reviewRepository.findById(twReviewNo).orElse(null);
            if (review != null) {
                imageService.deleteImageByUrl(review.getTwReviewContent());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "이미지 삭제 중 오류가 발생했습니다.";
        }

        // 리뷰 삭제
        reviewRepository.deleteById(twReviewNo);
        return "리뷰와 이미지가 성공적으로 삭제되었습니다.";
    }

    @Transactional(timeout = 60)
    public String reviewInsert(ReviewVO reviewVO, MultipartFile file) throws IOException {
        reviewRepository.save(reviewVO);

        if (file != null && !file.isEmpty()) {
            // 이미지를 S3에 업로드하고 DB에는 저장하지 않음
            imageService.uploadCloud("review", file);
        }

        return "Inserted review with id: " + reviewVO.getTwReviewNo();
    }

    public List<ReviewVO> reviewList() {
        return reviewRepository.findAll();
    }

    public Page<ReviewVO> searchReviews(String query, Pageable pageable) {
        return reviewRepository.findByTwReviewTitleContainingOrTwReviewContentContainingOrderByTwReviewNoDesc(query, query, pageable);
    }

    public List<ReviewVO> getRecentReviews() {
        return reviewRepository.findTop3ByOrderByTwReviewNoDesc();
    }
}
