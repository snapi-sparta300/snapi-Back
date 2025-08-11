package stparta300.snapi.domain.user.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stparta300.snapi.domain.model.enums.ChallengeState;
import stparta300.snapi.domain.user.entity.UserChallenge;

import java.util.List;
import java.util.Optional;

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


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select uc
        from UserChallenge uc
          join fetch uc.challenge c
          join fetch uc.user u
        where u.id = :userId
          and c.id = :challengeId
    """)
    Optional<UserChallenge> findForUpdate(@Param("userId") Long userId,
                                          @Param("challengeId") Long challengeId);

    Optional<UserChallenge> findByUser_IdAndChallenge_Id(Long userId, Long challengeId);

}