package travelwith.com.demo.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import travelwith.com.demo.image.ImageVO;
import travelwith.com.demo.image.ImageService;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ImageService imageService;

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
        imageService.deleteImage("review", String.valueOf(twReviewNo));

        reviewRepository.deleteById(twReviewNo);
        return "Deleted review with id: " + twReviewNo;
    }

    @Transactional(timeout = 60)
    public String reviewInsert(ReviewVO reviewVO, MultipartFile file) throws IOException {
        reviewRepository.save(reviewVO);

        if (file != null && !file.isEmpty()) {
            // MultipartFile 객체를 파일에 저장한다
            writeFile(file, reviewVO.getTwReviewNo());
        }

        return "Inserted review with id: " + reviewVO.getTwReviewNo();
    }

    private ImageVO writeFile(MultipartFile file, Long reviewId) throws IOException {
        // S3에 이미지 업로드
        String imageType = "review"; // 이미지 타입을 설정
        String fileUrl = imageService.uploadCloudAndSaveToDb(imageType, file, String.valueOf(reviewId));
        
        // ImageVO 객체 생성
        return ImageVO.builder()
                .imageType(imageType)
                .useId(String.valueOf(reviewId))
                .realFilename(fileUrl) // fileUrl을 사용하여 이미지 URL 저장
                .contentType(file.getContentType())
                .size(String.valueOf(file.getSize()))
                .createdAt(new java.util.Date())
                .build();
    }

    public List<ReviewVO> reviewList() {
        return reviewRepository.findAll();
    }
    
    public Page<ReviewVO> searchReviews(String query, Pageable pageable) {
        return reviewRepository.findByTwReviewTitleContainingOrTwReviewContentContainingOrderByTwReviewNoDesc(query, query, pageable);
    }
}
