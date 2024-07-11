package travelwith.com.demo.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import travelwith.com.demo.image.ImageService;
import travelwith.com.demo.image.ImageVO;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api1")
public class ReviewController {
    private final ReviewService reviewService;
    private final ImageService imageService;

    @PostMapping("/reviewView")
    @ResponseBody
    public Map<String, Object> reviewView(@RequestBody Map<String, String> request) {
        System.out.println("리뷰 상세보기 추출");
        Map<String, Object> result = new HashMap<>();
        try {
            Long twReviewNo = Long.parseLong(request.get("twReviewNo").toString());
            System.out.println("리뷰 번호: " + twReviewNo);
            ReviewVO review = reviewService.reviewDetail(twReviewNo);

            List<ImageVO> imageDetails = imageService.getImageDetails(String.valueOf(twReviewNo));
            int imageIndex = 0;
            for(ImageVO image: imageDetails) {
                String imageUrl = imageService.getImage("review", image.getRealFilename());
                result.put("imageUrl" + imageIndex, imageUrl);
                imageIndex++;
            }

            result.put("review", review);
            result.put("status", true);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", false);
            e.printStackTrace();
        }
        return result;
    }

    @PostMapping("/reviewInsert")
    public String reviewInsert(
            @RequestParam("twReviewTitle") String title,
            @RequestParam("twReviewContent") String content,
            @RequestParam("twReviewRating") String rating,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        ReviewVO reviewVO = ReviewVO.builder()
                .twReviewTitle(title)
                .twReviewContent(content)
                .twReviewRating(rating)
                .build();
        try {
            return reviewService.reviewInsert(reviewVO, file);
        } catch (IOException e) {
            e.printStackTrace();
            return "리뷰 등록에 실패했습니다.";
        }
    }

    @PostMapping("/reviewUpdate")
    @ResponseBody
    public Map<String, Object> reviewUpdate(@RequestBody Map<String, Object> request) {
        System.out.println("리뷰 정보 수정 요청 받음");
        Map<String, Object> result = new HashMap<>();
        try {
            Long twReviewNo = Long.parseLong(request.get("twReviewNo").toString());
            String twReviewTitle = (String) request.get("twReviewTitle");
            String twReviewContent = (String) request.get("twReviewContent");
            String twReviewRating = request.get("twReviewRating").toString();

            ReviewVO reviewVO = new ReviewVO();
            reviewVO.setTwReviewNo(twReviewNo);
            reviewVO.setTwReviewTitle(twReviewTitle);
            reviewVO.setTwReviewContent(twReviewContent);
            reviewVO.setTwReviewRating(twReviewRating);

            reviewService.reviewUpdate(reviewVO);

            result.put("review", reviewVO);
            result.put("status", true);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", false);
            e.printStackTrace();
        }
        return result;
    }

    @PostMapping("/reviewDelete")
    @ResponseBody
    public Map<String, Object> reviewDelete(@RequestBody Map<String, String> request) {
        System.out.println("리뷰 삭제 요청");
        Long twReviewNo = Long.parseLong(request.get("twReviewNo"));
        Map<String, Object> result = new HashMap<>();
        try {
            String deleteUser = reviewService.reviewDelete(twReviewNo);
            result.put("deleteReview", deleteUser);
            result.put("status", true);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", false);
            e.printStackTrace();
        }
        return result;
    }

    @GetMapping("/reviewList")
    @ResponseBody
    public Map<String, Object> reviewList(@PageableDefault(page = 0, size = 8) Pageable pageable) {
        System.out.println("리뷰 리스트 추출");
        Map<String, Object> result = new HashMap<>();
        try {
            Page<ReviewVO> reviewPages = reviewService.findAllPage(pageable);
            result.put("reviews", reviewPages.getContent());
            result.put("totalPages", reviewPages.getTotalPages());
            result.put("totalElements", reviewPages.getTotalElements());
            result.put("status", true);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", false);
            e.printStackTrace();
        }
        return result;
    }
    
    @GetMapping("/reviewSearch")
    public Map<String, Object> searchReviews(@RequestParam("query") String query) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<ReviewVO> reviews = reviewService.searchReviews(query);
            result.put("reviews", reviews);
            result.put("status", true);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", false);
            e.printStackTrace();
        }
        return result;
    }
}
