package stparta300.snapi.domain.mission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stparta300.snapi.domain.mission.entity.MissionImage;

import java.util.Collection;
import java.util.List;

public interface MissionImageRepository extends JpaRepository<MissionImage, Long> {
    List<MissionImage> findByMission_IdIn(Collection<Long> missionIds);
}