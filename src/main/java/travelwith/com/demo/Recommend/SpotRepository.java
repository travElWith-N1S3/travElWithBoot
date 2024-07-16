package travelwith.com.demo.Recommend;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpotRepository extends JpaRepository<Spot, Integer> {
    Spot findById(int id);

    List<Spot> findFirst3ByOrderByIdDesc();

    Page<Spot> findByTitleContainingOrContentsContaining(String title, String contents, Pageable pageable);
}