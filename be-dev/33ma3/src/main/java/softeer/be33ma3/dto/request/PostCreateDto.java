package softeer.be33ma3.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
@NoArgsConstructor
public class PostCreateDto {
    @NotBlank(message = "차종은 필수입니다.")
    private String carType;

    @NotBlank(message = "모델명은 필수입니다.")
    private String modelName;

    @NotNull(message = "마감 기한은 필수입니다.")
    @Max(value = 10, message = "최대 10일까지 가능합니다.")
    private Integer deadline;

    @NotBlank(message = "위치는 필수입니다.")
    private String location;

    private String repairService;

    private String tuneUpService;

    @NotNull(message = "반경 안 센터 정보는 필수입니다.")
    private List<Long> centers;

    @Length(max=500, message = "내용은 최대 500글자입니다.")
    @NotNull
    private String contents;

    @Builder
    public PostCreateDto(String carType, String modelName, Integer deadline, String location, String repairService,
                         String tuneUpService, List<Long> centers, String contents) {
        this.carType = carType;
        this.modelName = modelName;
        this.deadline = deadline;
        this.location = location;
        this.repairService = repairService;
        this.tuneUpService = tuneUpService;
        this.centers = centers;
        this.contents = contents;
    }
}
