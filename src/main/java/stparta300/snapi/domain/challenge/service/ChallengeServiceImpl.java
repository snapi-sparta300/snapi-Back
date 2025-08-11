package stparta300.snapi.domain.challenge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stparta300.snapi.domain.challenge.converter.ChallengeConverter;
import stparta300.snapi.domain.challenge.dto.response.ChallengeListResponse;
import stparta300.snapi.domain.challenge.dto.response.JoinChallengeResponse;
import stparta300.snapi.domain.challenge.entity.Challenge;
import stparta300.snapi.domain.challenge.repository.ChallengeRepository;
import stparta300.snapi.domain.model.enums.ChallengeState;
import stparta300.snapi.domain.user.converter.UserConverter;
import stparta300.snapi.domain.user.entity.User;
import stparta300.snapi.domain.user.entity.UserChallenge;
import stparta300.snapi.domain.user.handler.UserHandler;
import stparta300.snapi.domain.user.repository.UserChallengeRepository;
import stparta300.snapi.domain.user.repository.UserRepository;
import stparta300.snapi.global.error.code.status.ErrorStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeConverter challengeConverter;
    private final UserRepository userRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final UserConverter userConverter;

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
}