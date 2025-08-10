package stparta300.snapi.domain.mission.entity;

import jakarta.persistence.*;
import lombok.*;
import stparta300.snapi.domain.model.entity.BaseEntity;
import stparta300.snapi.domain.user.entity.UserMission;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "verified_image",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_verified_image_sha256", columnNames = "sha256_hash")
        },
        indexes = {
                @Index(name = "idx_verified_image_user_mission", columnList = "user_mission_id")
        })
public class VerifiedImage extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자-미션(검증 결과가 귀속되는 단위)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_mission_id", nullable = false)
    private UserMission userMission;

    @Column(nullable = false, length = 500)
    private String imageUrl;     // 검증된 S3 URL

    // AI 결과
    private Double confidence;   // 신뢰도

    @Column(length = 100)
    private String classDetected; // 감지 클래스

    @Column(length = 255)
    private String bbox;          // 바운딩 박스(JSON 문자열 등)

    @Column(name = "sha256_hash", nullable = false, length = 64)
    private String sha256Hash;    // 전역 유니크(중복 제출 방지)
}