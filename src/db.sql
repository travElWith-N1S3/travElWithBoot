-- 여행지 테이블
CREATE TABLE `tour-spot` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `images` varchar(255) DEFAULT null,
  `contents` varchar(1000) DEFAULT Null,
  PRIMARY KEY (`id`)
  ) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- 이미지 테이블
CREATE TABLE `travelwith_image` (
  `image_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `image_type` varchar(255) DEFAULT NULL,
  `use_id` varchar(255) DEFAULT NULL,
  `real_filename` varchar(255) DEFAULT NULL,
  `content_type` varchar(255) DEFAULT NULL,
  `size` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `image_url` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`image_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 리뷰테이블
CREATE TABLE `travelwith_review` (
  `tw_review_no` bigint(20) NOT NULL AUTO_INCREMENT,
  `tw_review_title` varchar(255) DEFAULT NULL,
  `tw_review_content` varchar(255) DEFAULT NULL,
  `tw_review_rating` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`tw_review_no`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;