package travelwith.com.demo.review;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

@Mapper
@Repository("reviewMapper")
public interface ReviewMapper {
	@Update("UPDATE travelwith_review SET tw_review_title = #{review.tw_review_title}, "
			+ "tw_review_content = #{review.tw_review_content}, " + "WHERE tw_review_no = #{review.tw_review_no}")
	void updateReview(@Param("review") ReviewVO reviewVO);

	@Select("SELECT * FROM travelwith_review WHERE tw_review_no = #{tw_review_no}")
	ReviewVO viewReview(@Param("tw_review_no") String tw_review_no);

	@Delete("DELETE FROM travelwith_review WHERE tw_review_no = #{tw_review_no}")
	void deleteReview(@Param("tw_review_no") String tw_review_no);

	@Select("SELECT * from travelwith_review")
	List<ReviewVO> getAllReviews();

	@Insert("INSERT INTO travelwith_review (tw_review_title, tw_review_content, tw_review_rating) VALUES (#{tw_review_title}, #{tw_review_content}, #{tw_review_rating})")
	int insertReview(Map<String, Object> contentData);
	// .
}