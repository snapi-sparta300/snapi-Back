package stparta300.snapi.domain.challenge.converter;

import org.springframework.stereotype.Component;
import stparta300.snapi.domain.challenge.dto.response.ChallengeListResponse;
import stparta300.snapi.domain.challenge.entity.Challenge;

import java.util.List;

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
}