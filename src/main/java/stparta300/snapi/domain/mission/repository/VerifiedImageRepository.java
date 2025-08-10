package stparta300.snapi.domain.mission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stparta300.snapi.domain.mission.entity.VerifiedImage;

import java.util.Optional;

public interface VerifiedImageRepository extends JpaRepository<VerifiedImage, Long> {
    boolean existsBySha256Hash(String sha256Hash);
    Optional<VerifiedImage> findBySha256Hash(String sha256Hash);
}