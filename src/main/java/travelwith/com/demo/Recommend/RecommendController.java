package travelwith.com.demo.Recommend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@Slf4j
@RequiredArgsConstructor
public class RecommendController {
    private final SpotService spotService;

    @GetMapping("/v1/popular-destination")
    public List<String> findPopularSpot(){
        return spotService.findPopularSpot();
    }

    @GetMapping("/v1/destination/info")
    public SpotDto getRecommendSpot(@RequestParam("id") int id) {
        return spotService.findById(id);
    }

    @GetMapping("/v1/destinationList")
    public Page<SpotDto> getAllSpot(@PageableDefault(page = 1) Pageable pageable) {
        return spotService.findAllPage(pageable);
    }

    @GetMapping("/v1/top-tour-spot")
    public List<SpotDto> get3spot(){
        return spotService.findTop3Spot();
    }
}
