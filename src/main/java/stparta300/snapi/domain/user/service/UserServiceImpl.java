package stparta300.snapi.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stparta300.snapi.domain.user.dto.request.LoginRequest;
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
}