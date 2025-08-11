package stparta300.snapi.domain.challenge.entity;

import jakarta.persistence.*;
import lombok.*;
import stparta300.snapi.domain.model.entity.BaseEntity;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "challenge")
public class Challenge extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    // 최대 참여 가능 횟수
    private Integer maxCount;

    // 현재 참여자/진행 수 등(ERD의 count)
    private Integer count;

    // 총 포인트(지급 총량 등)
    private Long totalPoint;

    @Column(length = 100)
    private String companyName;

    @Column(columnDefinition = "text")
    private String comment;

    // 전체 미션 개수
    private Integer totalMission;
}