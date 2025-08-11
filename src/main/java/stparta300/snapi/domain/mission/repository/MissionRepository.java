package stparta300.snapi.domain.mission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stparta300.snapi.domain.mission.entity.Mission;
import stparta300.snapi.domain.user.entity.UserMission;

import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {
}