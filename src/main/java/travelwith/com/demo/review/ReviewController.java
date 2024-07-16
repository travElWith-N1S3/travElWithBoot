package travelwith.com.demo.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import travelwith.com.demo.image.ImageService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api1")
public class ReviewController {
    private final ReviewService reviewService;
    private final ImageService imageService;

    @PostMapping("/reviewView")
    @ResponseBody
    public Map<String, Object> reviewView(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long twReviewNo = Long.parseLong(request.get("twReviewNo"));
            ReviewVO review = reviewService.reviewDetail(twReviewNo);
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
    public ResponseEntity<Map<String, Object>> reviewInsert(@RequestParam("twReviewTitle") String title,
            @RequestParam("twReviewContent") String content, @RequestParam("twReviewRating") String rating,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        ReviewVO reviewVO = ReviewVO.builder().twReviewTitle(title).twReviewContent(content).twReviewRating(rating)
                .build();
        try {
            CompletableFuture<String> insertResult = reviewService.reviewInsert(reviewVO, file);
            Map<String, Object> result = new HashMap<>();
            result.put("message", insertResult);
            result.put("status", true);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            e.printStackTrace();
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "리뷰 등록에 실패했습니다.");
            errorResult.put("status", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }

    @PostMapping("/reviewUpdate")
    @ResponseBody
    public Map<String, Object> reviewUpdate(@RequestBody Map<String, Object> request) {
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

    @GetMapping("/reviewSearch")
    public Map<String, Object> searchReviews(@RequestParam("query") String query,
                                             @RequestParam("page") int page,
                                             @RequestParam("size") int size) {
        Map<String, Object> result = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ReviewVO> reviews = reviewService.searchReviews(query, pageable);
            result.put("reviews", reviews.getContent());
            result.put("totalPages", reviews.getTotalPages());
            result.put("totalElements", reviews.getTotalElements());
            result.put("status", true);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", false);
            e.printStackTrace();
        }
        return result;
    }

    @GetMapping("/recentReviews")
    public Map<String, Object> getRecentReviews() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<ReviewVO> recentReviews = reviewService.getRecentReviews();
            result.put("recentReviews", recentReviews);
            result.put("status", true);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", false);
            e.printStackTrace();
        }
        return result;
    }

    @PostMapping("/imgUpload")
    public ResponseEntity<FileResponse> imgUpload(@RequestPart(value = "upload", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(FileResponse.builder().uploaded(false).build());
        }

        try {
            String imageUrl = imageService.writeFile(file);
            return ResponseEntity.ok().body(FileResponse.builder().uploaded(true).url(imageUrl).build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FileResponse.builder().uploaded(false).build());
        }
    }

}