package com.imageng.repository;

import com.imageng.entity.GeneratedImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneratedImageRepository extends JpaRepository<GeneratedImage, Long> {

    /**
     * Retrieves the most recent generated images up to 24 items.
     */
    List<GeneratedImage> findTop24ByOrderByTimestampDesc();

    /**
     * Retrieves all generated images ordered by newest first.
     */
    List<GeneratedImage> findAllByOrderByTimestampDesc();
}
