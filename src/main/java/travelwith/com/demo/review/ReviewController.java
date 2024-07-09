package travelwith.com.demo.review;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import travelwith.com.demo.image.CustomResponse;
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
            Long tw_review_no = Long.parseLong(request.get("tw_review_no"));
            System.out.println("리뷰 번호: " + tw_review_no);
            ReviewVO review = reviewService.reviewDetail(tw_review_no);

            List<ImageVO> imageDetails = imageService.getImageDetails(String.valueOf(tw_review_no));
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
            @RequestParam("tw_review_title") String title,
            @RequestParam("tw_review_content") String content,
            @RequestParam("tw_review_rating") String rating,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        ReviewVO reviewVO = ReviewVO.builder()
                .tw_review_title(title)
                .tw_review_content(content)
                .tw_review_rating(rating)
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
            Long tw_review_no = Long.parseLong((String) request.get("tw_review_no"));
            String tw_review_title = (String) request.get("tw_review_title");
            String tw_review_content = (String) request.get("tw_review_content");
            String tw_review_rating = (String) request.get("tw_review_rating");

            ReviewVO reviewVO = new ReviewVO();
            reviewVO.setTw_review_no(tw_review_no);
            reviewVO.setTw_review_title(tw_review_title);
            reviewVO.setTw_review_content(tw_review_content);
            reviewVO.setTw_review_rating(tw_review_rating);

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
        Long tw_review_no = Long.parseLong(request.get("tw_review_no"));
        Map<String, Object> result = new HashMap<>();
        try {
            String deleteUser = reviewService.reviewDelete(tw_review_no);
            result.put("deleteReview", deleteUser);
            result.put("status", true);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", false);
            e.printStackTrace();
        }
        return result;
    }

    @PostMapping("/reviewList")
    @ResponseBody
    public Map<String, Object> reviewList() {
        System.out.println("리뷰 리스트 추출");
        Map<String, Object> result = new HashMap<>();

        try {
            List<ReviewVO> list = reviewService.reviewList();
            result.put("list", list);
            result.put("status", true);
            System.out.println("result : " + result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", false);
            e.printStackTrace();
        }
        return result;
    }
}
