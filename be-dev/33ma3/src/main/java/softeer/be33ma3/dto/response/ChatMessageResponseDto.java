package softeer.be33ma3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import softeer.be33ma3.domain.ChatMessage;

import static softeer.be33ma3.utils.StringParser.createTimeParsing;

@Data
@AllArgsConstructor
public class ChatMessageResponseDto {
    private String contents;

    private String createTime;

    public static ChatMessageResponseDto create(ChatMessage chatMessage){
        return new ChatMessageResponseDto(chatMessage.getContents(), createTimeParsing(chatMessage.getCreateTime()));
    }
}
