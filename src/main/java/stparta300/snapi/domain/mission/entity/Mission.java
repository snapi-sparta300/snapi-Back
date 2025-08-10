package stparta300.snapi.domain.mission.entity;

import jakarta.persistence.*;
import lombok.*;
import stparta300.snapi.domain.challenge.entity.Challenge;
import stparta300.snapi.domain.model.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mission",
        indexes = @Index(name = "idx_mission_challenge", columnList = "challenge_id"))
public class Mission extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: Challenge
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(nullable = false, length = 100)
    private String name;

    // 미션 수행 시 지급 포인트
    @Column(nullable = false)
    private Long point;
}