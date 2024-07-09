package travelwith.com.demo.review;

import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "travelwith_review")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewVO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tw_review_no;  // 기본 키를 Long 타입으로 변경
    private String tw_review_title;
    private String tw_review_content;
    private String tw_review_rating;
    
    @Transient
    private MultipartFile file;
}
