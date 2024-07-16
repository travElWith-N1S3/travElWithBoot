package travelwith.com.demo.Recommend;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpotService {
	private final SpotRepository spotRepository;
	private final RedisTemplate<String, String> redisStrTemplate;

	@PostConstruct
	public void addDummyData() {
		redisStrTemplate.opsForZSet().add("popular", "제주도 한라산", 1);
		redisStrTemplate.opsForZSet().add("popular", "부산 해운대", 1);
		redisStrTemplate.opsForZSet().add("popular", "전주 한옥마을", 1);
		redisStrTemplate.opsForZSet().add("popular", "서울 강남", 7);
		redisStrTemplate.opsForZSet().add("popular", "동대문 DDP", 6);
		redisStrTemplate.opsForZSet().add("popular", "인천 차이나타운", 5);
		redisStrTemplate.opsForZSet().add("popular", "영남 알프스", 4);
		redisStrTemplate.opsForZSet().add("popular", "서울 인사동", 3);
		redisStrTemplate.opsForZSet().add("popular", "경주 석굴암", 2);
		redisStrTemplate.opsForZSet().add("popular", "서울 경복궁", 1);
		redisStrTemplate.opsForZSet().incrementScore("popular", "제주도 한라산", 10);
		redisStrTemplate.opsForZSet().incrementScore("popular", "부산 해운대", 9);
		redisStrTemplate.opsForZSet().incrementScore("popular", "전주 한옥마을", 8);
	}

	public List<String> findPopularSpot() {
		Set<String> popular = redisStrTemplate.opsForZSet().reverseRange("popular", 0, 5);
		System.out.println(popular);
		return new ArrayList<>(popular);
	}

	public SpotDto findById(int id) {
		return new SpotDto(spotRepository.findById(id));
	}

	public Page<SpotDto> findAllPage(String query, Pageable pageable) {
        return spotRepository.findByTitleContainingOrContentsContaining(query, query, pageable)
                .map(SpotDto::new);
    }

	public List<SpotDto> findTop3Spot() {
		return spotRepository.findFirst3ByOrderByIdDesc().stream().map(SpotDto::new).collect(Collectors.toList());
	}

}
