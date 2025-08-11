package stparta300.snapi.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stparta300.snapi.domain.model.enums.ChallengeState;
import stparta300.snapi.domain.user.entity.UserChallenge;

import java.util.List;

public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {
    boolean existsByUser_IdAndChallenge_Id(Long userId, Long challengeId);

    @Query("""
        select uc
        from UserChallenge uc
          join fetch uc.challenge c
        where uc.user.id = :userId
          and uc.state = :state
        order by uc.createdAt desc
    """)
    List<UserChallenge> findAllByUserIdAndStateWithChallenge(@Param("userId") Long userId,
                                                             @Param("state") ChallengeState state);

}