// src/main/java/stparta300/snapi/domain/mission/service/MissionService.java
package stparta300.snapi.domain.mission.service;

import org.springframework.web.multipart.MultipartFile;
import stparta300.snapi.domain.mission.dto.response.MissionImageResponse;

public interface MissionService {
    MissionImageResponse uploadMissionImage(Long challengeId, Long missionId, Long userId, MultipartFile file);
}