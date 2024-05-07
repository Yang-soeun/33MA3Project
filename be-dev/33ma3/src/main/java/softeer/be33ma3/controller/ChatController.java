package softeer.be33ma3.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import softeer.be33ma3.domain.Member;
import softeer.be33ma3.dto.response.ChatHistoryDto;
import softeer.be33ma3.dto.response.AllChatRoomDto;
import softeer.be33ma3.jwt.CurrentUser;
import softeer.be33ma3.response.DataResponse;
import softeer.be33ma3.service.ChatService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/chatRoom/{post_id}/{center_id}")
    public ResponseEntity<?> createRoom(@CurrentUser Member member, @PathVariable("center_id") Long centerId,
                                        @PathVariable("post_id") Long postId) {
        Long roomId = chatService.createRoom(member, centerId, postId);

        return ResponseEntity.ok().body(DataResponse.success("채팅방 생성 성공", roomId));
    }

    @GetMapping("/chatRoom/all")
    public ResponseEntity<?> showAllChatRoom(@CurrentUser Member member) {
        List<AllChatRoomDto> allChatRoomDtos = chatService.showAllChatRoom(member);

        return ResponseEntity.ok().body(DataResponse.success("문의 내역 전송 성공", allChatRoomDtos));
    }

    @GetMapping("/chat/history/{room_id}")
    public ResponseEntity<?> showOneChatHistory(@CurrentUser Member member, @PathVariable("room_id") Long roomId) {
        List<ChatHistoryDto> chatHistoryDtos = chatService.showOneChatHistory(member, roomId);

        return ResponseEntity.ok().body(DataResponse.success("채팅 내역 전송 성공", chatHistoryDtos));
    }
}
