package stparta300.snapi.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stparta300.snapi.domain.user.entity.UserChallenge;

public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {
    boolean existsByUser_IdAndChallenge_Id(Long userId, Long challengeId);
}