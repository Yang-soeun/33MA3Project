package softeer.be33ma3.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatMessageRequestDto {
    @NotBlank(message = "메세지는 필수입니다.")
    private String message;
}
