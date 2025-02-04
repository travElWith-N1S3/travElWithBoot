package travelwith.com.demo.Recommend;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.ToString;

@Entity
@Getter
@Table(name = "tour_spot")
@ToString
public class Spot {

    @Id
    private int id;
    private String title;
    private String images;
    private String contents;
}
