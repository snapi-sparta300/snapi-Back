package stparta300.snapi.domain.user.converter;

import org.springframework.stereotype.Component;
import stparta300.snapi.domain.challenge.dto.response.ActiveChallengesResponse;
import stparta300.snapi.domain.challenge.dto.response.JoinChallengeResponse;
import stparta300.snapi.domain.user.dto.request.SignupRequest;
import stparta300.snapi.domain.user.dto.response.*;
import stparta300.snapi.domain.user.entity.User;
import stparta300.snapi.domain.user.entity.UserChallenge;
import stparta300.snapi.domain.user.entity.UserMission;
import stparta300.snapi.domain.model.enums.Gender;
import stparta300.snapi.domain.user.dto.request.ProfileSetupRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class UserConverter {
    public LoginResponse toLoginResponse(Long userId) {
        return LoginResponse.builder().userId(userId).build();
    }

    // 평문 저장(요청에 맞춤). 운영 전환 시 인코딩으로 쉽게 교체 가능.
    public User toUser(SignupRequest req) {
        return User.builder()
                .userName(req.getUsername())
                .password(req.getPassword())
                .term(false)          // 기본값(동의 절차 없으면 false)
                .userPoint(0L)        // 기본 0포인트
                .build();
    }

    public SignupResponse toSignupResponse(User saved) {
        return SignupResponse.builder()
                .userId(saved.getId())
                .username(saved.getUserName())
                .build();
    }


    public void applyProfile(User user, ProfileSetupRequest req) {
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        user.setGender(parseGender(req.getGender()));
        user.setBirth(parseDate(req.getBirth()));
        user.setTerm(Boolean.TRUE.equals(req.getTerm()));
    }

    public ProfileSetupResponse toProfileSetupResponse(User user) {
        return ProfileSetupResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .gender(renderGender(user.getGender()))
                .birth(user.getBirth() != null ? user.getBirth().toString() : null)
                .userPoint(user.getUserPoint())
                .build();
    }

    private Gender parseGender(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.equals("남자")) return Gender.MALE;
        if (v.equals("여자")) return Gender.FEMALE;
        // 프로젝트 Gender enum 값에 맞게 필요 시 추가 분기
        throw new IllegalArgumentException("gender는 '남자' 또는 '여자'만 허용합니다.");
    }

    private String renderGender(Gender gender) {
        if (gender == null) return null;
        switch (gender) {
            case MALE: return "남자";
            case FEMALE: return "여자";
            default: return gender.name();
        }
    }

    private LocalDate parseDate(String yyyyMMdd) {
        try {
            return LocalDate.parse(yyyyMMdd);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("birth 형식은 YYYY-MM-DD 여야 합니다.");
        }
    }


    public UserDetailResponse toUserDetail(User u) {
        return UserDetailResponse.builder()
                .userId(u.getId())
                .nickname(u.getNickname())
                .email(u.getEmail())
                .gender(renderGender(u.getGender()))
                .birth(u.getBirth() != null ? u.getBirth().toString() : null)
                .userPoint(u.getUserPoint())
                .build();
    }

    public UpdateMemberResponse toUpdateResponse(User u) {
        return UpdateMemberResponse.builder()
                .userId(u.getId())
                .userName(u.getUserName())
                .email(u.getEmail())
                .nickname(u.getNickname())
                .gender(renderGender(u.getGender()))
                .birth(u.getBirth() != null ? u.getBirth().toString() : null)
                .term(u.isTerm())
                .userPoint(u.getUserPoint())
                .build();
    }
    public PointHistoryResponse toPointHistoryResponse(
            User user,
            List<UserMission> histories,
            long totalEarned
    ) {
        DateTimeFormatter iso = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        List<PointHistoryResponse.HistoryDto> items = histories.stream()
                .map(um -> PointHistoryResponse.HistoryDto.builder()
                        .userMissionId(um.getId())
                        .challengeId(um.getChallenge().getId())
                        .missionId(um.getMission().getId())
                        .missionName(um.getMission().getName())  // Mission.name
                        .point(um.getMission().getPoint())       // Mission.point
                        .state(um.getState().name())             // PASS/IN_PROGRESS/FAIL
                        .createdAt(um.getCreatedAt() != null ? um.getCreatedAt().format(iso) : null)
                        .build())
                .toList();

        return PointHistoryResponse.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .currentPoint(user.getUserPoint())
                .totalEarned(totalEarned)
                .histories(items)
                .build();
    }


    public ActiveChallengesResponse toActiveChallengesResponse(User user, List<UserChallenge> list) {
        List<ActiveChallengesResponse.Item> items = list.stream().map(uc -> {
            var c = uc.getChallenge();
            long totalMission = c.getTotalMission() != null ? c.getTotalMission().longValue() : 0L;
            long success = uc.getSuccessMission() != null ? uc.getSuccessMission() : 0L;
            double progress = (totalMission > 0) ? ((double) success / (double) totalMission) : 0.0;

            Long maxCount = c.getMaxCount() != null ? c.getMaxCount().longValue() : 0L;
            Long current = c.getCount() != null ? c.getCount().longValue() : 0L;

            return ActiveChallengesResponse.Item.builder()
                    .userChallengeId(uc.getId())
                    .challengeId(c.getId())
                    .name(c.getName())
                    .companyName(c.getCompanyName())
                    .comment(c.getComment())
                    .totalMission(totalMission)
                    .successMission(success)
                    .progress(progress)
                    .maxCount(maxCount)
                    .currentCount(current)
                    .totalPoint(c.getTotalPoint())
                    .state("진행중") // 명세 고정값
                    .build();
        }).toList();

        return ActiveChallengesResponse.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .challenges(items)
                .build();
    }

    public JoinChallengeResponse toJoinChallengeResponse(User user, UserChallenge uc) {
        return JoinChallengeResponse.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .userChallengeId(uc.getId())
                .challengeId(uc.getChallenge().getId())
                .build();
    }
}