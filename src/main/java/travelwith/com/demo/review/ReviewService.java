package travelwith.com.demo.review;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
	private final ReviewMapper reviewMapper;

	public ReviewVO reviewUpdate(ReviewVO reviewVO) {
		try {
			reviewMapper.updateReview(reviewVO);
			return reviewVO;
		} catch (Exception e) {
			System.err.println("Error updating review: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public int reviewInsert(Map<String, Object> contentData) {
		try {
			int status = reviewMapper.insertReview(contentData);
			return status;
		} catch (Exception e) {
			System.err.println("Error fetching board insert: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public ReviewVO reviewDetail(String tw_review_no) {
		try {
			return reviewMapper.viewReview(tw_review_no);
		} catch (Exception e) {
			System.err.println("Error viewing review: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public String reviewDelete(String tw_review_no) {
		try {
			reviewMapper.deleteReview(tw_review_no);
			return tw_review_no;
		} catch (Exception e) {
			System.err.println("Error deleting review: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public List<ReviewVO> reviewList() {
		try {
			List<ReviewVO> reviewList = reviewMapper.getAllReviews();
			return reviewList;
		} catch (Exception e) {
			System.err.println("Error fetching select options: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	// .
}