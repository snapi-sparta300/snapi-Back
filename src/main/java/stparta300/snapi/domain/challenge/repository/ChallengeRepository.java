package stparta300.snapi.domain.challenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stparta300.snapi.domain.challenge.entity.Challenge;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> { }