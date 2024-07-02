package com.tour.Recommend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
public class RecommendController {
    private final SpotRepository spotRepository;

    @GetMapping("/v1/spot")
    public List<SpotDto> getRecommendSpot(){
        List<SpotDto> all = spotRepository.findAll().stream()
                .map(SpotDto::new)
                .collect(Collectors.toList());
        return all;
    }
}
