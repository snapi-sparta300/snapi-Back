package stparta300.snapi.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stparta300.snapi.domain.user.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String userName);
    boolean existsByEmail(String email);
 