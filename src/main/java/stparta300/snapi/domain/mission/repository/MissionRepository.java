package stparta300.snapi.domain.mission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stparta300.snapi.domain.mission.entity.Mission;

public interface MissionRepository extends JpaRepository<Mission, Long> { }