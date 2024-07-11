package travelwith.com.demo.image;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ImageService {
	private final AmazonS3 amazonS3Client; // S3

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	public String getImage(String image_type, String real_filename) {
		String key = image_type + "/" + real_filename;
		return amazonS3Client.getUrl(bucketName, key).toString();
	}

	public String uploadCloud(String image_type, MultipartFile file) throws IOException {
		// 서버에 저장할 파일 이름을 생성 UUID
		String realFileName = UUID.randomUUID().toString();

		// 파일 메타데이터 설정
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(file.getSize());
		objectMetadata.setContentType(file.getContentType());

		// 저장될 위치 + 파일명
		String key = image_type + "/" + realFileName;

		// 클라우드에 파일 저장
		amazonS3Client.putObject(bucketName, key, file.getInputStream(), objectMetadata);
		amazonS3Client.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);

		return amazonS3Client.getUrl(bucketName, key).toString();
	}

	public String writeFile(MultipartFile file) throws IOException {
		// S3에 이미지 업로드
		String imageType = "review"; // 이미지 타입을 설정
		String fileUrl = uploadCloud(imageType, file);

		return fileUrl;
	}

	// 정규표현식: <img> 태그에서 src 속성 값을 추출
	private static final Pattern IMG_SRC_PATTERN = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");

	public void deleteImageByUrl(String reviewContent) {
		Matcher matcher = IMG_SRC_PATTERN.matcher(reviewContent);
		while (matcher.find()) {
			String imageUrl = matcher.group(1);
			// 이미지 URL에서 S3 버킷 내 키 추출
			String key = extractKeyFromImageUrl(imageUrl);
			// S3에서 이미지 삭제
			amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, key));
		}
	}

	// 이미지 URL에서 S3 버킷 내 키 추출 메서드
	private String extractKeyFromImageUrl(String imageUrl) {
		int startIndex = imageUrl.indexOf(bucketName) + bucketName.length() + 1;
		return imageUrl.substring(startIndex);
	}
}
