package stparta300.snapi.domain.mission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stparta300.snapi.domain.mission.entity.TempImage;

import java.util.List;

public interface TempImageRepository extends JpaRepository<TempImage, Long> {
    List<TempImage> findByUserMission_Id(Long userMissionId);
    boolean existsBySha256Hash(String sha256Hash);

}