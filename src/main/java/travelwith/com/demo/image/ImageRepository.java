package travelwith.com.demo.image;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<ImageVO, Long> {
    List<ImageVO> findByUseId(String use_id);
    void deleteByUseId(String use_id);
}
