package softeer.be33ma3.dto.request.member;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CenterSignUpDto extends ClientSignUpDto {

    @NotNull(message = "위도는 필수입니다.")
    private double latitude;

    @NotNull(message = "경도는 필수입니다.")
    private double longitude;

    public CenterSignUpDto(String loginId, String password, double latitude, double longitude) {
        super(loginId, password);
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
