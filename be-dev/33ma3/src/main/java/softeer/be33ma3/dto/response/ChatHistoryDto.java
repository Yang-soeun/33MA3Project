package softeer.be33ma3.dto.response;

import lombok.Builder;
import lombok.Data;
import softeer.be33ma3.domain.ChatMessage;

import java.time.LocalDateTime;

import static softeer.be33ma3.utils.StringParser.createTimeParsing;

@Data
@Builder
public class ChatHistoryDto {
    private Long senderId;

    private String contents;

    private String createTime;

    private boolean readDone;

    public static ChatHistoryDto getChatHistoryDto(ChatMessage chatMessage, LocalDateTime createTime){
        return ChatHistoryDto.builder()
                .senderId(chatMessage.getSender().getMemberId())
                .contents(chatMessage.getContents())
                .createTime(createTimeParsing(createTime))
                .readDone(chatMessage.isReadDone())
                .build();
    }
}
