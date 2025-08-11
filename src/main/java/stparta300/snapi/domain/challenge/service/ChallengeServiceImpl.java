package stparta300.snapi.domain.challenge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stparta300.snapi.domain.challenge.converter.ChallengeConverter;
import stparta300.snapi.domain.challenge.dto.response.ChallengeDetailResponse;
import stparta300.snapi.domain.challenge.dto.response.ChallengeListResponse;
import stparta300.snapi.domain.challenge.dto.response.CompleteChallengeResponse;
import stparta300.snapi.domain.challenge.dto.response.JoinChallengeResponse;
import stparta300.snapi.domain.challenge.entity.Challenge;
import stparta300.snapi.domain.challenge.repository.ChallengeRepository;
import stparta300.snapi.domain.mission.entity.Mission;
import stparta300.snapi.domain.mission.entity.MissionImage;
import stparta300.snapi.domain.mission.repository.MissionImageRepository;
import stparta300.snapi.domain.mission.repository.MissionRepository;
import stparta300.snapi.domain.model.enums.ChallengeState;
import stparta300.snapi.domain.model.enums.UserMissionState;
import stparta300.snapi.domain.user.converter.UserConverter;
import stparta300.snapi.domain.user.entity.User;
import stparta300.snapi.domain.user.entity.UserChallenge;
import stparta300.snapi.domain.user.entity.UserMission;
import stparta300.snapi.domain.user.handler.UserHandler;
import stparta300.snapi.domain.user.repository.UserChallengeRepository;
import stparta300.snapi.domain.user.repository.UserMissionRepository;
import stparta300.snapi.domain.user.repository.UserRepository;
import stparta300.snapi.global.error.code.status.ErrorStatus;
import stparta300.snapi.global.exception.GeneralException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeConverter challengeConverter;
    private final UserRepository userRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final UserConverter userConverter;
    private final MissionRepository missionRepository;
    private final MissionImageRepository missionImageRepository;
    private final UserMissionRepository userMissionRepository;
    private final ChallengeConverter converter;

    @Override
    public ChallengeListResponse getChallenges() {
        List<Challenge> all = challengeRepository.findAll(); // 명세: 전체 목록
        return challengeConverter.toChallengeListResponse(all);
    }


    @Override
    @Transactional
    public JoinChallengeResponse join(Long userId, Long challengeId) {
        // 1) 사용자/챌린지 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND)); // 404
        Challenge challenge = challengeRepository.findByIdForUpdate(challengeId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.CHALLENGE_NOT_FOUND)); // 404

        // 2) 중복 참여 방지 (Unique 보장)
        if (userChallengeRepository.existsByUser_IdAndChallenge_Id(userId, challengeId)) {
            throw new UserHandler(ErrorStatus.CHALLENGE_ALREADY_JOINED); // 409 (선택: 명세 외 추가 보호)
        }

        // 3) 정원 체크 (maxCount 초과 시 409)
        Integer maxCount = challenge.getMaxCount();            // 엔티티 게터 타입에 맞춤
        int current = challenge.getCount() == null ? 0 : challenge.getCount();

        if (maxCount != null && current >= maxCount) {
            throw new UserHandler(ErrorStatus.CHALLENGE_CAPACITY_FULL); // 409
        }

        // 4) UserChallenge 생성 (진행중/성공미션=0)
        UserChallenge uc = UserChallenge.builder()
                .user(user)
                .challenge(challenge)
                .state(ChallengeState.IN_PROGRESS)  // "진행중"
                .successMission(0L)
                .build();
        userChallengeRepository.save(uc);

        // 5) 현재 참여 인원 +1
        challenge.setCount(current + 1);

        // 6) 응답 변환
        return userConverter.toJoinChallengeResponse(user, uc);
    }


    @Override
    @Transactional
    public CompleteChallengeResponse complete(Long userId, Long challengeId) {
        // 1) 존재 검증 (User는 조인으로도 가져오지만, 에러 메시지 통일을 위해 미리 검증 가능)
        userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 2) 참여 관계 + 잠금 조회
        UserChallenge uc = userChallengeRepository.findForUpdate(userId, challengeId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_CHALLENGE_NOT_FOUND));

        // 3) 이미 완료 여부
        if (uc.getState() == ChallengeState.COMPLETED) {
            throw new UserHandler(ErrorStatus.CHALLENGE_ALREADY_COMPLETED);
        }

        // 4) 미션 개수 비교 (타입 차이 안전 변환)
        long success = numToLong(uc.getSuccessMission());
        long total = numToLong(uc.getChallenge().getTotalMission());
        if (success != total) {
            // 409: 모든 미션 완료 전에는 챌린지 완료 불가
            throw new UserHandler(ErrorStatus.CHALLENGE_NOT_ALL_MISSIONS_DONE);
        }

        // 5) 상태 완료 전환
        uc.markCompleted();

        // 6) 포인트 적립 (챌린지 총 포인트)
        long award = numToLong(uc.getChallenge().getTotalPoint());
        long before = numToLong(uc.getUser().getUserPoint());
        uc.getUser().setUserPoint(before + award);

        // 7) 응답
        return userConverter.toCompleteChallengeResponse(uc, award);
    }

    private long numToLong(Number n) { return n == null ? 0L : n.longValue(); }

    @Override
    public ChallengeDetailResponse getChallengeDetail(Long challengeId, Long userId) {
        // 1) 챌린지
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CHALLENGE_NOT_FOUND));

        // 2) 미션 목록
        List<Mission> missions = missionRepository.findAllByChallengeId(challengeId);

        // 3) 대표 이미지 (있는 경우만 맵핑) - N+1 회피: 한번에 조회
        Map<Long, String> firstImageUrlByMissionId = Map.of();
        if (!missions.isEmpty()) {
            List<Long> missionIds = missions.stream().map(Mission::getId).toList();
            List<MissionImage> images = missionImageRepository.findByMission_IdIn(missionIds);
            firstImageUrlByMissionId = converter.toFirstImageMap(images);
        }

        // 4) 유저별 참여/상태 맵 & 요약
        Map<Long, UserMission> userMissionMap = Collections.emptyMap();
        ChallengeDetailResponse.UserStatusDto userStatus = null;
        if (userId != null) {
            List<UserMission> userMissions = userMissionRepository
                    .findByUser_IdAndChallenge_Id(userId, challengeId);
            userMissionMap = converter.toUserMissionMap(userMissions);

            UserChallenge uc = userChallengeRepository
                    .findByUser_IdAndChallenge_Id(userId, challengeId)
                    .orElse(null);
            userStatus = converter.toUserStatus(uc);
        }

        // 5) 변환
        ChallengeDetailResponse dto =
                converter.toDetail(challenge, missions, userMissionMap, firstImageUrlByMissionId, userStatus);

        // 6) 전체 달성률 계산 (%): PASS 수 / (참여자수 * 총미션수) * 100
        long totalMission = dto.getTotalMission() == null ? 0L : dto.getTotalMission();
        long participantCount = dto.getCurrentCount() == null ? 0L : dto.getCurrentCount();
        if (totalMission > 0 && participantCount > 0) {
            long passCount = userMissionRepository
                    .countByChallenge_IdAndState(challengeId, UserMissionState.PASS);
            double rate = (double) passCount / (participantCount * (double) totalMission) * 100.0;
            // 리플렉션 말고, DTO 재생성으로 값만 교체
            dto = ChallengeDetailResponse.builder()
                    .challengeId(dto.getChallengeId())
                    .name(dto.getName())
                    .companyName(dto.getCompanyName())
                    .comment(dto.getComment())
                    .totalPoint(dto.getTotalPoint())
                    .maxCount(dto.getMaxCount())
                    .currentCount(dto.getCurrentCount())
                    .progressRate(rate)
                    .totalMission(dto.getTotalMission())
                    .missions(dto.getMissions())
                    .userStatus(dto.getUserStatus())
                    .build();
        }

        return dto;
    }
}