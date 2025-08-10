package stparta300.snapi.domain.mission.entity;

import jakarta.persistence.*;
import lombok.*;
import stparta300.snapi.domain.model.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mission_image",
        indexes = @Index(name = "idx_mission_image_mission", columnList = "mission_id"))
public class MissionImage extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 미션 기준 이미지(프롬프트 등)
    @Column(nullable = false, length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;
}