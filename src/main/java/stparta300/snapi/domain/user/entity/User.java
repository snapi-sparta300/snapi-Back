package stparta300.snapi.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import stparta300.snapi.domain.model.entity.BaseEntity;
import stparta300.snapi.domain.model.enums.Gender;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "user_name", unique = true),
        @Index(name = "idx_user_email", columnList = "email", unique = true)
})
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", nullable = false, length = 50, unique = true)
    private String userName;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    private LocalDate birth;

    // 서비스 이용약관 동의
    @Column(nullable = false)
    private boolean term;

    // 적립 포인트
    @Column(nullable = false)
    private Long userPoint;

    public void plusPoint(long delta) { this.userPoint = this.userPoint + delta; }
    public void minusPoint(long delta) { this.userPoint = Math.max(0, this.userPoint - del