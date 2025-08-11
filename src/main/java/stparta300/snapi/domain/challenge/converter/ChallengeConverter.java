package stparta300.snapi.domain.challenge.converter;

import org.springframework.stereotype.Component;
import stparta300.snapi.domain.challenge.dto.response.ChallengeDetailResponse;
import stparta300.snapi.domain.challenge.dto.response.ChallengeListResponse;
import stparta300.snapi.domain.challenge.entity.Challenge;
import stparta300.snapi.domain.mission.entity.Mission;
import stparta300.snapi.domain.mission.entity.MissionImage;
import stparta300.snapi.domain.user.entity.UserChallenge;
import stparta300.snapi.domain.user.entity.UserMission;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ChallengeConverter {

    public ChallengeListResponse toChallengeListResponse(List<Challenge> list) {
        List<ChallengeListResponse.Item> items = list.stream().map(c ->
                ChallengeListResponse.Item.builder()
                        .challengeId(c.getId())
                        .name(c.getName())
                        .companyName(c.getCompanyName())
                        .comment(c.getComment())
                        .totalMission(c.getTotalMission() != null ? c.getTotalMission().longValue() : 0L)
                        .maxCount(getSafeLong(getMaxCount(c)))   // 엔티티에 없을 수 있어 안전 처리
                        .currentCount(c.getCount() != null ? c.getCount().longValue() : 0L)
                        .totalPoint(c.getTotalPoint() != null ? c.getTotalPoint() : 0L)
                        .build()
        ).toList();

        return ChallengeListResponse.builder()
                .challenges(items)
                .build();
    }

    // --- 안전 보조 메서드 ---
    private Long getMaxCount(Challenge c) {
        try {
            // 필드가 존재한다면 Lombok 게터가 생성되어 있을 것이라 가정
            return (Long) Challenge.class.getMethod("getMaxCount").invoke(c);
        } catch (Throwable ignore) {
            return null; // 엔티티에 maxCount가 없으면 null
        }
    }
    private Long getSafeLong(Long v) { return v == null ? 0L : v; }


    public ChallengeDetailResponse toDetail(
            Challenge challenge,
            List<Mission> missions,
            Map<Long, UserMission> userMissionByMissionId,
            Map<Long, String> firstImageUrlByMissionId,
            ChallengeDetailResponse.UserStatusDto userStatus
    ) {
        List<ChallengeDetailResponse.MissionDto> missionDtos = missions.stream().map(m -> {
            var um = userMissionByMissionId.get(m.getId());
            String imageUrl = firstImageUrlByMissionId.get(m.getId()); // 없으면 null -> 직렬화 제외
            return ChallengeDetailResponse.MissionDto.builder()
                    .missionId(m.getId())
                    .name(m.getName())
                    .point(numToLong(m.getPoint()))
                    .imageUrl(imageUrl)
                    .userJoined(um != null)
                    .userMissionId(um != null ? um.getId() : null)
                    .userState(um != null ? um.getState().name() : null)
                    .build();
        }).toList();

        long totalMission = missions.size();
        double progressRate = 0.0;
        // 전체 달성률 = (해당 챌린지의 PASS UserMission 수) / (현재 참여 인원 * 총 미션 수) * 100
        // 계산은 Service에서 전달받은 값으로 세팅하도록 Service가 채워 넣도록 해도 됨.
        // 여기서는 0.0 초기값만 둔다가 Service에서 set 한다.
        return ChallengeDetailResponse.builder()
                .challengeId(challenge.getId())
                .name(challenge.getName())
                .companyName(challenge.getCompanyName())
                .comment(challenge.getComment())
                .totalPoint(numToLong(challenge.getTotalPoint()))
                .maxCount(numToLong(challenge.getMaxCount()))
                .currentCount(numToLong(challenge.getCount()))
                .progressRate(progressRate)
                .totalMission(totalMission)
                .missions(missionDtos)
                .userStatus(userStatus)
                .build();
    }

    public ChallengeDetailResponse.UserStatusDto toUserStatus(UserChallenge uc) {
        if (uc == null) return null;
        long success = numToLong(uc.getSuccessMission());
        long total = numToLong(uc.getChallenge().getTotalMission());
        double progress = total > 0 ? (double) success / (double) total : 0.0;
        return ChallengeDetailResponse.UserStatusDto.builder()
                .userChallengeId(uc.getId())
                .state(uc.getState().name())
                .successMission(success)
                .totalMission(total)
                .progress(progress)
                .build();
    }

    public Map<Long, String> toFirstImageMap(List<MissionImage> images) {
        // missionId -> 가장 먼저 등장한 imageUrl (정렬 기준은 repo 반환 순서)
        return images.stream()
                .collect(Collectors.toMap(
                        img -> img.getMission().getId(),
                        MissionImage::getImageUrl,
                        (a, b) -> a // 중복일 때 첫 번째 유지
                ));
    }

    public Map<Long, UserMission> toUserMissionMap(List<UserMission> userMissions) {
        return userMissions.stream().collect(Collectors.toMap(
                um -> um.getMission().getId(),
                Function.identity(),
                (a, b) -> a // 같은 missionId 중복 방지
        ));
    }

    private long numToLong(Number n) { return n == null ? 0L : n.longValue(); }

}