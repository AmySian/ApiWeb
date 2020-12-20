package com.Apiweb.polling.depot;
import com.Apiweb.polling.model.Sondage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SondageDepot extends JpaRepository<Sondage, Long>{
    Optional<Sondage> findById(Long sondageId);

    Page<Sondage> findByCreatedBy(Long userId, Pageable pageable);

    long countByCreatedBy(Long userId);

    List<Sondage> findByIdIn(List<Long> sondageIds);

    List<Sondage> findByIdIn(List<Long> sondageIds, Sort sort);


}
