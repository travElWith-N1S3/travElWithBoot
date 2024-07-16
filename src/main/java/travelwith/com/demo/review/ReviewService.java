package travelwith.com.demo.review;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import travelwith.com.demo.image.ImageService;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ImageService imageService;
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${api.gateway.url}")
    private String apiGatewayUrl;

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
        try {
            ReviewVO review = reviewRepository.findById(twReviewNo).orElse(null);
            if (review != null) {
                imageService.deleteImageByUrl(review.getTwReviewContent());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "이미지 삭제 중 오류가 발생했습니다.";
        }

        reviewRepository.deleteById(twReviewNo);
        return "리뷰와 이미지가 성공적으로 삭제되었습니다.";
    }

    @Transactional(timeout = 60)
    public Mono<String> reviewInsert(ReviewVO reviewVO, MultipartFile file) throws IOException {
        reviewRepository.save(reviewVO);

        if (file != null && !file.isEmpty()) {
            imageService.uploadCloud("review", file);
        }

        String title = reviewVO.getTwReviewTitle();
        String rank = reviewVO.getTwReviewRating();
        String body = reviewVO.getTwReviewContent();

        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(apiGatewayUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        WebClient client = webClientBuilder
                .uriBuilderFactory(factory)
                .baseUrl(apiGatewayUrl)
                .build();

        return client.post()
                .body(BodyInserters.fromValue(new ReviewPayload(title, rank, body)))
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(ex -> ex.printStackTrace())
                .onErrorReturn("An error occurred while inserting the review.");
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