package travelwith.com.demo.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewVO {
	public String tw_review_no;
	public String tw_review_title;
	public String tw_review_content;
	public String tw_review_rating;
}