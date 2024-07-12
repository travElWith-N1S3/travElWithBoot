package travelwith.com.demo.Recommend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpotRepository extends JpaRepository<Spot, Integer> {
//    List<Spot> findAll();
    Spot findById(int id);

    List<Spot> findFirst3ByOrderByIdDesc();
}