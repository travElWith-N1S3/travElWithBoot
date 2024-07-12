package travelwith.com.demo.scheduler;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import travelwith.com.demo.review.ReviewVO;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class SchedulerService {

    private final AmazonS3 amazonS3Client;
    private static final Pattern IMG_SRC_PATTERN = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
    
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @PersistenceContext
    private EntityManager entityManager;

    @Scheduled(fixedDelay = 60000*10) // 10분마다 실행
    @Transactional
    public void fileTokenAutoDelete() {
        log.info("사용되지 않는 이미지 파일을 삭제합니다.");
        
        // S3에서 사용되지 않는 이미지 파일 삭제
        deleteUnusedImagesFromS3();
    }

    private void deleteUnusedImagesFromS3() {
        List<String> allImagesInContent = getAllImagesInContent();
        List<String> allImagesInS3 = getAllImagesInS3();

        // S3에 있고 콘텐츠에 없는 파일을 찾는다
        List<String> unusedImages = allImagesInS3.stream()
            .filter(image -> !allImagesInContent.contains(image))
            .collect(Collectors.toList());

        for (String unusedImage : unusedImages) {
            deleteFileFromS3(unusedImage);
        }
    }

    private List<String> getAllImagesInContent() {
        List<ReviewVO> allReviews = entityManager.createQuery("SELECT r FROM ReviewVO r", ReviewVO.class).getResultList();
        return allReviews.stream()
            .flatMap(review -> {
                Matcher matcher = IMG_SRC_PATTERN.matcher(review.getTwReviewContent());
                return matcher.results()
                    .map(match -> {
                        String src = match.group(1);
                        // 파일 경로만 추출 (파일 이름뿐만 아니라 경로 전체)
                        return src.substring(src.indexOf("review/"));
                    })
                    .filter(src -> src != null && !src.isEmpty())
                    .collect(Collectors.toList())
                    .stream();
            })
            .collect(Collectors.toList());
    }

    private List<String> getAllImagesInS3() {
        ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request().withBucketName(bucketName);
        ListObjectsV2Result listObjectsV2Result = amazonS3Client.listObjectsV2(listObjectsV2Request);
        return listObjectsV2Result.getObjectSummaries().stream()
            .map(S3ObjectSummary::getKey)
            .collect(Collectors.toList());
    }

    private void deleteFileFromS3(String fileName) {
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, fileName);
        amazonS3Client.deleteObject(deleteObjectRequest);
        log.info("S3에서 파일 삭제됨: " + fileName);
    }
}
