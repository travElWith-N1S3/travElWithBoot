package travelwith.com.demo.Recommend;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpotService {

    private final SpotRepository spotRepository;

    public SpotDto findById(int id){
        return new SpotDto( spotRepository.findById(id));
    }

    public Page<SpotDto> findAllPage(Pageable pageable){
        int page = Math.max(0, pageable.getPageNumber() - 1); // 페이지 번호가 음수가 되지 않도록 처리
        int pageLimit = 8;
        Page<Spot> postsPages = spotRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
        Page<SpotDto> spotDtos = postsPages.map(postsPage -> new SpotDto(postsPage));
        return spotDtos;
    }

    public List<SpotDto> findTop3Spot(){
        return spotRepository.findFirst3ByOrderByIdDesc()
                .stream().map(SpotDto::new)
                .collect(Collectors.toList());
    }
}
