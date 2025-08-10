package stparta300.snapi.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import stparta300.snapi.domain.challenge.entity.Challenge;
import stparta300.snapi.domain.mission.entity.Mission;
import stparta300.snapi.domain.model.entity.BaseEntity;
import stparta300.snapi.domain.model.enums.UserMissionState;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_mission",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_mission_user_mission",
                        columnNames = {"user_id", "mission_id"})
        },
        indexes = {
                @Index(name = "idx_user_mission_user", columnList = "user_id"),
                @Index(name = "idx_user_mission_challenge", columnList = "challenge_id"),
                @Index(name = "idx_user_mission_mission", columnList = "mission_id")
        })
public class UserMission extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //冗長하지만 ERD 그대로: user_mission에서 challenge도 직접 참조
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserMissionState state;
}