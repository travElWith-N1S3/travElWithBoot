package travelwith.com.demo.Recommend;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class SpotDto {
    private int id;
    private String title;
    private List<String> images;
    private String contents;

    public SpotDto(Spot entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.images = new ArrayList<>(List.of(entity.getImages().split(",")));
        this.contents = entity.getContents();
    }
}
