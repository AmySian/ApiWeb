package com.Apiweb.polling.depot;
import com.Apiweb.polling.model.ChoixVoteCount;
import com.Apiweb.polling.model.Vote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VoteDepot extends JpaRepository<Vote, Long> {
    @Query("SELECT NEW com.Apiweb.polling.model.ChoixVoteCount(v.choix.id, count(v.id)) FROM Vote v WHERE v.sondage.id in :sondageIds GROUP BY v.sondage.id")
    List<ChoixVoteCount> countBySondageIdInGroupByChoixId(@Param("sondageIds") List<Long> sondageIds);

    @Query("SELECT NEW com.Apiweb.polling.model.ChoixVoteCount(v.sondage.id, count(v.id)) FROM Vote v WHERE v.sondage.id = :sondageId GROUP BY v.sondage.id")
    List<ChoixVoteCount> countBySondageIdGroupByChoixId(@Param("sondageId") Long sondageId);

    @Query("SELECT v FROM Vote v where v.user.id = :userId and v.sondage.id in :sondageIds")
    List<Vote> findByUserIdAndSondageIdIn(@Param("userId") Long userId, @Param("sondageIds") List<Long> sondageIds);

    @Query("SELECT v FROM Vote v where v.user.id = :userId and v.sondage.id = :sondageId")
    Vote findByUserIdAndSondageId(@Param("userId") Long userId, @Param("sondageId") Long pollId);

    @Query("SELECT COUNT(v.id) from Vote v where v.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT v.sondage.id FROM Vote v WHERE v.user.id = :userId")
    Page<Long> findVotedSondageIdsByUserId(@Param("userId") Long userId, Pageable pageable);
}
