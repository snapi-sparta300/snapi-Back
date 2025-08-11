// src/main/java/stparta300/snapi/domain/mission/controller/MissionController.java
package stparta300.snapi.domain.mission.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import stparta300.snapi.domain.mission.dto.response.MissionImageResponse;
import stparta300.snapi.domain.mission.service.MissionService;
import stparta300.snapi.global.error.code.status.SuccessStatus;
import stparta300.snapi.global.common.response.ApiResponse;

@Tag(name = "mission", description = "미션 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping
public class MissionController {

    private final MissionService missionService;

    @io.swagger.v3.oas.annotations.Operation(
            summary = "미션참여(이미지 업로드 + 중복검사)",
            description = "SHA-256 중복 검사 → S3 업로드 → UserMission/TempImage 갱신 → Flask 검증(동기)"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    @PostMapping(
            value = "/challenges/{challengeId}/missions/{missionId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse uploadMissionImage( // 👈 여기의 ApiResponse는 우리 프로젝트 클래스
                                           @PathVariable Long challengeId,
                                           @PathVariable Long missionId,
                                           @RequestParam("userId") Long userId,
                                           @RequestPart("file") MultipartFile file
    ) {
        MissionImageResponse result =
                missionService.uploadMissionImage(challengeId, missionId, userId, file);

        return ApiResponse.onSuccess(SuccessStatus.MISSION_IMAGE_UPLOAD_SUCCESS, result);
    }
}