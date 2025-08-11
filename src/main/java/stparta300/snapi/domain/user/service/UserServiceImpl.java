package stparta300.snapi.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stparta300.snapi.domain.challenge.dto.response.ActiveChallengesResponse;
import stparta300.snapi.domain.model.enums.ChallengeState;
import stparta300.snapi.domain.model.enums.UserMissionState;
import stparta300.snapi.domain.user.converter.UserConverter;
import stparta300.snapi.domain.user.dto.request.LoginRequest;
import stparta300.snapi.domain.user.dto.request.ProfileSetupRequest;
import stparta300.snapi.domain.user.dto.request.SignupRequest;
import stparta300.snapi.domain.user.dto.response.*;
import stparta300.snapi.domain.user.entity.User;
import stparta300.snapi.domain.user.entity.UserChallenge;
import stparta300.snapi.domain.user.entity.UserMission;
import stparta300.snapi.domain.user.handler.UserHandler;
import stparta300.snapi.domain.user.repository.UserChallengeRepository;
import stparta300.snapi.domain.user.repository.UserMissionRepository;
import stparta300.snapi.domain.user.repository.UserRepository;
import stparta300.snapi.global.error.code.status.ErrorStatus;
import org.springframework.dao.DataIntegrityViolationException;
import stparta300.snapi.domain.model.enums.Gender;
import stparta300.snapi.domain.user.dto.request.UpdateMemberRequest;


import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;   // findByUserName(...) 사용
    private final PasswordEncoder passwordEncoder; // BCrypt
    private final UserConverter userConverter;
    private final UserMissionRepository userMissionRepository;
    private final UserChallengeRepository userChallengeRepository;

    @Override
    public Long login(LoginRequest request) {
        User user = userRepository.findByUserName(request.getUsername())
                .orElseThrow(() -> new UserHandler(ErrorStatus.LOGIN_BAD_CREDENTIALS));

        // 평문 비교로 변경
        if (!user.getPassword().equals(request.getPassword())) {
            throw new UserHandler(ErrorStatus.LOGIN_BAD_CREDENTIALS);
        }

        return user.getId();
    }


    @Override
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByUserName(request.getUsername())) {
            // 메시지: "회원가입 실패. 아이디 중복"
            throw new UserHandler(ErrorStatus.SIGNUP_USERNAME_DUPLICATED);
        }
        User saved = userRepository.save(userConverter.toUser(request));
        return userConverter.toSignupResponse(saved);
    }

    @Override
    @Transactional
    public ProfileSetupResponse setupProfile(Long userId, ProfileSetupRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND)); // 적절한 에러 상수 사용/추가

        try {
            userConverter.applyProfile(user, request); // 필드 업데이트
        } catch (IllegalArgumentException e) {
            // gender, birth 포맷 오류 등을 400으로
            throw new UserHandler(ErrorStatus.PROFILE_BAD_REQUEST);
        }

        // 변경감지로 update 반영
        return userConverter.toProfileSetupResponse(user);
    }


    @Override
    public UserDetailResponse getMemberProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));
        return userConverter.toUserDetail(user);
    }

    @Override
    @Transactional
    public UpdateMemberResponse updateMember(Long userId, UpdateMemberRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // nickname
        if (req.getNickname() != null) {
            String nn = req.getNickname().trim();
            if (nn.isEmpty()) throw new UserHandler(ErrorStatus.MEMBER_UPDATE_BAD_REQUEST);
            user.setNickname(nn);
        }

        // email
        if (req.getEmail() != null) {
            String email = normalizeEmail(req.getEmail());
            if (email.isEmpty()) throw new UserHandler(ErrorStatus.MEMBER_UPDATE_BAD_REQUEST);
            if (userRepository.existsByEmailAndIdNot(email, userId)) {
                throw new UserHandler(ErrorStatus.MEMBER_EMAIL_DUPLICATED);
            }
            user.setEmail(email);
        }

        // gender
        if (req.getGender() != null) {
            user.setGender(parseGender(req.getGender()));
        }

        // birth
        if (req.getBirth() != null) {
            user.setBirth(parseBirth(req.getBirth()));
        }

        // term
        if (req.getTerm() != null) {
            user.setTerm(req.getTerm());
        }

        try {
            // JPA Dirty Checking → update
            return userConverter.toUpdateResponse(user);
        } catch (DataIntegrityViolationException e) {
            // DB 유니크 경합 대비
            throw new UserHandler(ErrorStatus.MEMBER_EMAIL_DUPLICATED);
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private Gender parseGender(String value) {
        String v = value.trim();
        if (v.equals("남자")) return Gender.MALE;
        if (v.equals("여자")) return Gender.FEMALE;
        throw new UserHandler(ErrorStatus.MEMBER_UPDATE_BAD_REQUEST);
    }

    private LocalDate parseBirth(String yyyyMMdd) {
        try { return LocalDate.parse(yyyyMMdd); }
        catch (DateTimeParseException e) { throw new UserHandler(ErrorStatus.MEMBER_UPDATE_BAD_REQUEST); }
    }

    @Override
    public PointHistoryResponse getPointHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

        UserMissionState state = UserMissionState.PASS; // 기본: 완료만
        List<UserMission> histories = userMissionRepository
                .findAllByUserIdAndStateWithMission(userId, state);

        Long totalEarned = userMissionRepository
                .sumPointByUserIdAndState(userId, state);

        return userConverter.toPointHistoryResponse(user, histories, totalEarned != null ? totalEarned : 0L);
    }

    @Override
    public ActiveChallengesResponse getActiveChallenges(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.MEMBER_NOT_FOUND));

        List<UserChallenge> list = userChallengeRepository
                .findAllByUserIdAndStateWithChallenge(userId, ChallengeState.IN_PROGRESS);

        return userConverter.toActiveChallengesResponse(user, list);
    }



}


