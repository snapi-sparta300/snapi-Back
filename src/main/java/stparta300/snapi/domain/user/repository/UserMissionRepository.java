package stparta300.snapi.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stparta300.snapi.domain.model.enums.UserMissionState;
import stparta300.snapi.domain.user.entity.UserMission;

import java.util.List;
import java.util.Optional;

public interface UserMissionRepository extends JpaRepository<UserMission, Long> {
    Optional<UserMission> findByUser_IdAndMission_Id(Long userId, Long missionId);
    boolean existsByUser_IdAndMission_Id(Long userId, Long missionId);


    @Query("""
        select um
        from UserMission um
            join fetch um.mission m
        where um.user.id = :userId
          and um.state = :state
        order by um.createdAt desc
    """)
    List<UserMission> findAllByUserIdAndStateWithMission(@Param("userId") Long userId,
                                                         @Param("state") UserMissionState state);

    @Query("""
        select coalesce(sum(m.point), 0)
        from UserMission um
            join um.mission m
        where um.user.id = :userId
          and um.state = :state
    """)
    Long sumPointByUserIdAndState(@Param("userId") Long userId,
                                  @Param("state") UserMissionState state);

    List<UserMission> findByUser_IdAndChallenge_Id(Long userId, Long challengeId);

    long countByChallenge_IdAndState(Long challengeId,
                                     stparta300.snapi.domain.model.enums.UserMissionState state);
}