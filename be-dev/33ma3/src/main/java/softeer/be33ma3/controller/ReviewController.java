package softeer.be33ma3.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import softeer.be33ma3.domain.Member;
import softeer.be33ma3.dto.request.ReviewCreateDto;
import softeer.be33ma3.dto.response.ShowCenterReviewsDto;
import softeer.be33ma3.dto.response.ShowAllReviewDto;
import softeer.be33ma3.jwt.CurrentUser;
import softeer.be33ma3.response.DataResponse;
import softeer.be33ma3.response.SingleResponse;
import softeer.be33ma3.service.ReviewService;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;


    @PostMapping("/{post_id}")
    public ResponseEntity<?> createReview(@PathVariable("post_id") Long postId,
                                          @RequestBody @Valid ReviewCreateDto reviewCreateDto,
                                          @CurrentUser Member member) {
        Long reviewId = reviewService.createReview(postId, reviewCreateDto, member);
        return ResponseEntity.ok().body(DataResponse.success("센터 리뷰 작성 성공", reviewId));
    }


    @DeleteMapping("/{review_id}")
    public ResponseEntity<?> deleteReview(@PathVariable("review_id") Long reviewId, @CurrentUser Member member) {
        reviewService.deleteReview(reviewId, member);
        return ResponseEntity.ok().body(SingleResponse.success("센터 리뷰 삭제 성공"));
    }

    @GetMapping
    public ResponseEntity<?> showAllReview() {
        List<ShowAllReviewDto> showAllReviewDtos = reviewService.showAllReview();

        return ResponseEntity.ok().body(DataResponse.success("전체 리뷰 조회 성공", showAllReviewDtos));
    }

    @GetMapping("/{center_id}")
    public ResponseEntity<?> showOneCenterReview(@PathVariable("center_id") Long centerId) {
        ShowCenterReviewsDto showCenterReviewsDtos = reviewService.showOneCenterReviews(centerId);

        return ResponseEntity.ok().body(DataResponse.success("센터 리뷰 조회 성공", showCenterReviewsDtos));
    }
}
