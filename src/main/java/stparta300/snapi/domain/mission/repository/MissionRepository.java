package stparta300.snapi.domain.mission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stparta300.snapi.domain.mission.entity.Mission;
import stparta300.snapi.domain.user.entity.UserMission;

import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    @Query("""
        select m
        from Mission m
        where m.challenge.id = :challengeId
        order by m.id asc
    """)
    List<Mission> findAllByChallengeId(@Param("challengeId") Long challengeId);

}