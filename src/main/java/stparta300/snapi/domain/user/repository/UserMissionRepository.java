package stparta300.snapi.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stparta300.snapi.domain.user.entity.UserMission;

import java.util.Optional;

public interface UserMissionRepository extends JpaRepository<UserMission, Long> {
    Optional<UserMission> findByUser_IdAndMission_Id(Long userId, Long missionId);
    boolean existsByUser_IdAndMission_Id(Long userId, Long missionId);
}