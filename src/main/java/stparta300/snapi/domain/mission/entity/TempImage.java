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
@Table(name = "temp_image",
        indexes = {
                @Index(name = "idx_temp_image_user_mission", columnList = "user_mission_id"),
                @Index(name = "idx_temp_image_sha256", columnList = "sha256_hash")
        })
public class TempImage extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자-미션(제출 단위)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_mission_id", nullable = false)
    private UserMission userMission;

    // 미션 번호(챌린지 내 순번 등 필요 시)
    @Column(name = "mission_number")
    private Integer missionNumber;

    @Column(nullable = false, length = 500)
    private String imageUrl;     // S3 URL

    @Column(name = "sha256_hash", length = 64)
    private String sha256Hash;   // 중복 체크 참고용(Verified와 다른 정책)
}