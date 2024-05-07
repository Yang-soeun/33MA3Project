package softeer.be33ma3.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import softeer.be33ma3.domain.Member;
import softeer.be33ma3.dto.request.OfferCreateDto;
import softeer.be33ma3.dto.response.OfferDetailDto;
import softeer.be33ma3.jwt.CurrentUser;
import softeer.be33ma3.response.DataResponse;
import softeer.be33ma3.response.SingleResponse;
import softeer.be33ma3.service.OfferService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/post")
public class OfferController {

    private final OfferService offerService;

    @GetMapping("/{post_id}/offer/{offer_id}")
    public ResponseEntity<?> showOffer(@PathVariable("post_id") Long postId, @PathVariable("offer_id") Long offerId) {
        OfferDetailDto offerDetailDto = offerService.showOffer(postId, offerId);

        return ResponseEntity.ok(DataResponse.success("견적 불러오기 성공", offerDetailDto));
    }

    @PostMapping("/{post_id}/offer")
    public ResponseEntity<?> createOffer(@PathVariable("post_id") Long postId,
                                         @RequestBody @Valid OfferCreateDto offerCreateDto,
                                         @CurrentUser Member member) {
        Long offerId = offerService.createOffer(postId, offerCreateDto, member);

        return ResponseEntity.ok(DataResponse.success("입찰 성공", offerId));
    }

    @PatchMapping("/{post_id}/offer/{offer_id}")
    public ResponseEntity<?> updateOffer(@PathVariable("post_id") Long postId, @PathVariable("offer_id") Long offerId,
                                         @RequestBody @Valid OfferCreateDto offerCreateDto,
                                         @CurrentUser Member member) {
        offerService.updateOffer(postId, offerId, offerCreateDto, member);

        return ResponseEntity.ok(DataResponse.success("댓글 수정 성공", offerId));
    }

    @DeleteMapping("/{post_id}/offer/{offer_id}")
    public ResponseEntity<?> deleteOffer(@PathVariable("post_id") Long postId, @PathVariable("offer_id") Long offerId,
                                         @CurrentUser Member member) {
        offerService.deleteOffer(postId, offerId, member);

        return ResponseEntity.ok(DataResponse.success("댓글 삭제 성공", offerId));
    }

    @GetMapping("/{post_id}/offer/{offer_id}/select")
    public ResponseEntity<?> selectOffer(@PathVariable("post_id") Long postId, @PathVariable("offer_id") Long offerId,
                                         @CurrentUser Member member) {
        offerService.selectOffer(postId, offerId, member);

        return ResponseEntity.ok(SingleResponse.success("낙찰 완료, 게시글 마감"));
    }
}
