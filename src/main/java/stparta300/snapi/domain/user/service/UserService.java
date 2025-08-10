package stparta300.snapi.domain.user.service;

import stparta300.snapi.domain.user.dto.request.LoginRequest;

public interface UserService {
    Long login(LoginRequest request); // 성공 시 userId 반환
}