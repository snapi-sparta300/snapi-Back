package stparta300.snapi.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stparta300.snapi.domain.user.converter.UserConverter;
import stparta300.snapi.domain.user.dto.request.LoginRequest;
import stparta300.snapi.domain.user.dto.request.ProfileSetupRequest;
import stparta300.snapi.domain.user.dto.request.SignupRequest;
import stparta300.snapi.domain.user.dto.response.ProfileSetupResponse;
import stparta300.snapi.domain.user.dto.response.SignupResponse;
import stparta300.snapi.domain.user.entity.User;
import stparta300.snapi.domain.user.handler.UserHandler;
import stparta300.snapi.domain.user.repository.UserRepository;
import stparta300.snapi.global.error.code.status.ErrorStatus;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;   // findByUserName(...) 사용
    private final PasswordEncoder passwordEncoder; // BCrypt
    private final UserConverter userConverter;

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


}


