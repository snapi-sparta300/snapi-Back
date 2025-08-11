package stparta300.snapi.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import stparta300.snapi.domain.challenge.entity.Challenge;
import stparta300.snapi.domain.model.entity.BaseEntity;
import stparta300.snapi.domain.model.enums.ChallengeState;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_challenge",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_challenge_user_challenge",
                        columnNames = {"user_id", "challenge_id"})
        },
        indexes = {
                @Index(name = "idx_user_challenge_user", columnList = "user_id"),
                @Index(name = "idx_user_challenge_challenge", columnList = "challenge_id")
        })
public class UserChallenge extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChallengeState state;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 성공 미션 개수
    @Column(nullable = false)
    private Long successMission;

    public void markCompleted() {
        this.state = ChallengeState.COMPLETED;  // enum 값 이름 확인 필요(아래 참고)
        // BaseEntity가 자동으로 updatedAt 갱신해주면 생략, 아니면 필요 시 수동 갱신
        // this.updatedAt = LocalDateTime.now();
    }

}