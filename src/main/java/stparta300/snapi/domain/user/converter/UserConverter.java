package stparta300.snapi.domain.user.converter;

import org.springframework.stereotype.Component;
import stparta300.snapi.domain.user.dto.response.LoginResponse;

@Component
public class UserConverter {
    public LoginResponse toLoginResponse(Long userId) {
        return LoginResponse.builder().userId(userId).build();
    }
}