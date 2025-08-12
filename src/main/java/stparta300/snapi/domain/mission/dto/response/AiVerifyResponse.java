package stparta300.snapi.domain.mission.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiVerifyResponse {
    private boolean success;
    private Long  challengeId;   // 응답이 "1" 같은 문자열이므로 String 권장
    private Long  missionId;
    private Long    userId;
    private Long    tempImageId;
    private String  imageUrl;

    @JsonProperty("Confidence")
    private Double  confidence;

    private Boolean classDetected; // true/false
    private List<Double> bbox;     // [x1,y1,x2,y2]
}