package travelwith.com.demo.review;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api1")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/reviewView")
    @ResponseBody
    public Map<String, Object> reviewView(@RequestBody Map<String, String> request) {
        System.out.println("리뷰 상세보기 추출");
        Map<String, Object> result = new HashMap<>();
        try {
            String tw_review_no = request.get("tw_review_no");
            System.out.println("리뷰 번호: " + tw_review_no);
            ReviewVO review = reviewService.reviewDetail(tw_review_no);
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
    @ResponseBody
    public String reviewInsert(@RequestBody ReviewVO reviewVO) {
        System.out.println("리뷰 등록");
        Map<String, Object> result = new HashMap<>();

        try {
            String status = reviewService.reviewInsert(reviewVO);
            result.put("status", true);
            return "성공";
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", false);
            e.printStackTrace();
            return "실패";
        }
    }

    @PostMapping("/reviewUpdate")
    @ResponseBody
    public Map<String, Object> reviewUpdate(@RequestBody ReviewVO reviewVO) {
        System.out.println("리뷰 정보 수정 요청 받음");
        Map<String, Object> result = new HashMap<>();
        try {
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
        String tw_review_no = request.get("tw_review_no");
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
