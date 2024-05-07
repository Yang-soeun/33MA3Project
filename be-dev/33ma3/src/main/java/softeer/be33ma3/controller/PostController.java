package softeer.be33ma3.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import softeer.be33ma3.domain.Member;
import softeer.be33ma3.dto.request.PostCreateDto;
import softeer.be33ma3.dto.response.PostThumbnailDto;
import softeer.be33ma3.jwt.CurrentUser;
import softeer.be33ma3.response.DataResponse;
import softeer.be33ma3.response.SingleResponse;
import softeer.be33ma3.service.PostService;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {
    private final PostService postService;

    @GetMapping
    public ResponseEntity<?> showPosts(@RequestParam(name = "mine", required = false) Boolean mine,
                                       @RequestParam(name = "done", required = false) Boolean done,
                                       @RequestParam(name = "region", required = false) String region,
                                       @RequestParam(name = "repair", required = false) String repair,
                                       @RequestParam(name = "tuneUp", required = false) String tuneUp,
                                       @CurrentUser Member member) {
        List<PostThumbnailDto> postThumbnailDtos = postService.showPosts(mine, done, region, repair, tuneUp, member);

        return ResponseEntity.ok().body(DataResponse.success("게시글 목록 조회 성공", postThumbnailDtos));
    }


    @PostMapping(value = "/create")
    public ResponseEntity<?> createPost(@CurrentUser Member member,
                                        @RequestPart(name = "images", required = false) List<MultipartFile> images,
                                        @Valid @RequestPart(name = "request") PostCreateDto postCreateDto) {
        Long postId = postService.createPost(member, postCreateDto, images);

        return ResponseEntity.ok().body(DataResponse.success("게시글 작성 성공", postId));
    }


    @GetMapping("/one/{post_id}")
    public ResponseEntity<?> showPost(@PathVariable("post_id") Long postId, @CurrentUser Member member) {
        Object result = postService.showPost(postId, member);

        return ResponseEntity.ok(DataResponse.success("게시글 조회 완료", result));
    }


    @PutMapping("/{post_id}")
    public ResponseEntity<?> editPost(@CurrentUser Member member, @PathVariable("post_id") Long postId,
                                      @RequestBody @Valid PostCreateDto postCreateDto) {
        postService.editPost(member, postId, postCreateDto);

        return ResponseEntity.ok().body(SingleResponse.success("게시글 수정 성공"));
    }

    @DeleteMapping("/{post_id}")
    public ResponseEntity<?> deletePost(@CurrentUser Member member, @PathVariable("post_id") Long postId) {
        postService.deletePost(member, postId);

        return ResponseEntity.ok().body(SingleResponse.success("게시글 삭제 성공"));
    }
}
