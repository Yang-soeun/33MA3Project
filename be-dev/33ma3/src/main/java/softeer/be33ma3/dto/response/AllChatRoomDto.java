package softeer.be33ma3.dto.response;

import lombok.Data;
import softeer.be33ma3.domain.ChatRoom;

import java.time.LocalDateTime;

import static softeer.be33ma3.utils.StringParser.createTimeParsing;

@Data
public class AllChatRoomDto {

    private Long roomId;

    private Long centerId;

    private String centerProfile;

    private Long clientId;

    private String clientProfile;

    private String memberName;      // 상대방 이름

    private String lastMessage;     // 마지막 메세지

    private int noReadCount;        // 읽지 않은 메세지 개수

    private String createTime;

    public static AllChatRoomDto create(ChatRoom chatRoom, String lastMessage, String memberName, int noReadCount, LocalDateTime createTime) {
        AllChatRoomDto allChatRoomDto = new AllChatRoomDto();
        allChatRoomDto.roomId = chatRoom.getChatRoomId();
        allChatRoomDto.centerId = chatRoom.getCenter().getMemberId();
        allChatRoomDto.centerProfile = chatRoom.getCenter().getImage();
        allChatRoomDto.clientId = chatRoom.getClient().getMemberId();
        allChatRoomDto.clientProfile = chatRoom.getClient().getImage();
        allChatRoomDto.memberName = memberName;
        allChatRoomDto.lastMessage = lastMessage;
        allChatRoomDto.noReadCount = noReadCount;
        allChatRoomDto.createTime = createTimeParsing(createTime);

        return allChatRoomDto;
    }
}
