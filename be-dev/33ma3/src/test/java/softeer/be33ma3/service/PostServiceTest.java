package softeer.be33ma3.service;

import java.util.Collection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import softeer.be33ma3.domain.*;
import softeer.be33ma3.dto.request.PostCreateDto;
import softeer.be33ma3.dto.response.PostThumbnailDto;
import softeer.be33ma3.dto.response.PostWithAvgPriceDto;
import softeer.be33ma3.dto.response.PostWithOffersDto;
import softeer.be33ma3.exception.BusinessException;
import softeer.be33ma3.exception.ErrorCode;
import softeer.be33ma3.repository.*;

import java.util.ArrayList;
import java.util.List;
import softeer.be33ma3.repository.offer.OfferRepository;
import softeer.be33ma3.repository.post.PostRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static softeer.be33ma3.exception.ErrorCode.LOGIN_REQUIRED;
import static softeer.be33ma3.exception.ErrorCode.NOT_FOUND_POST;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostServiceTest {
    public static final String LOCATION = "서울시 강남구";

    @Autowired private MockMvc mockMvc;
    @Autowired private PostService postService;
    @Autowired private PostRepository postRepository;
    @Autowired private PostPerCenterRepository postPerCenterRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private RegionRepository regionRepository;
    @Autowired private ImageRepository imageRepository;
    @Autowired private OfferRepository offerRepository;
    @Autowired private CenterRepository centerRepository;
    @MockBean private ImageService imageService;

    @BeforeEach
    void setUp(){
        //회원 저장
        Member member1 = Member.createClient( "client1", "1234", null);
        Member member2 = Member.createClient( "client2", "1234", null);
        Member member3 = Member.createCenter( "center1", "1234", null);
        Member member4 = Member.createCenter( "center2", "1234", null);
        memberRepository.saveAll(List.of(member1, member2, member3, member4));
        regionRepository.save(new Region(1L, "강남구"));
    }

    @AfterEach
    void tearDown(){
        postPerCenterRepository.deleteAllInBatch();
        centerRepository.deleteAllInBatch();
        imageRepository.deleteAllInBatch();
        offerRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        regionRepository.deleteAllInBatch();
    }

    @DisplayName("게시글을 생성할 수 있다. - 이미지 포함")
    @Test
    void createPostWithImage(){
        //given
        MultipartFile mockMultipartFile1 = Mockito.mock(MultipartFile.class);
        List<MultipartFile> multipartFiles = List.of(mockMultipartFile1);

        Member member = memberRepository.findMemberByLoginId("client1").get();
        PostCreateDto postCreateDto = createPostDto(LOCATION,"게시글 생성 이미지 포함", "기스, 깨짐", "오일 교체");

        //when
        Long postId = postService.createPost(member, postCreateDto, multipartFiles);

        //then
        verify(imageService, times(1)).saveImages((anyList()));
        Post post = postRepository.findById(postId).get();
        assertThat(post).extracting("carType", "modelName", "deadline", "repairService", "tuneUpService", "contents")
                .containsExactly("승용차", "제네시스", 3, "기스, 깨짐", "오일 교체", "게시글 생성 이미지 포함");
    }

    @DisplayName("게시글을 생성할 수 있다. - 이미지 미포함")
    @Test
    void createPostWithoutImage(){
        //given
        Member member = memberRepository.findMemberByLoginId("client1").get();
        PostCreateDto postCreateDto = createPostDto(LOCATION,"게시글 생성 이미지 미포함", "기스, 깨짐", "오일 교체");

        //when
        Long postId = postService.createPost(member, postCreateDto, null);

        //then
        Post post = postRepository.findById(postId).get();
        assertThat(post).extracting("carType", "modelName", "deadline", "repairService", "tuneUpService", "contents")
                .containsExactly("승용차", "제네시스", 3, "기스, 깨짐", "오일 교체", "게시글 생성 이미지 미포함");
    }

    @DisplayName("게시글 작성 시 존재하지 않는 지역인 경우 예외가 발생한다.")
    @Test
    void createPostWithUnknownRegion(){
        //given
        Member member = memberRepository.findMemberByLoginId("center1").get();
        PostCreateDto postCreateDto = createPostDto("서울시 없는구", "게시글 작성 불가능", "기스, 깨짐", "오일 교체");

        //when  //then
        assertThatThrownBy(() -> postService.createPost(member, postCreateDto, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_CREATION_DISABLED);
    }

    @DisplayName("센터가 글을 작성하면 예외가 발생한다.")
    @Test
    void createPostWithCenter(){
        //given
        Member member = memberRepository.findMemberByLoginId("center1").get();
        PostCreateDto postCreateDto = new PostCreateDto();

        //when  //then
        assertThatThrownBy(() -> postService.createPost(member, postCreateDto, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_CREATION_DISABLED);
    }

    @DisplayName("게시글을 수정할 수 있다.")
    @Test
    void editPost(){
        //given
        Member member = memberRepository.findMemberByLoginId("client1").get();
        Region region = regionRepository.findByRegionName("강남구").get();
        Post savedPost = savePost(region, member,"기스, 깨짐", "오일 교체");
        PostCreateDto postEditDto = createPostDto(LOCATION,"수정 후 내용", "기스, 깨짐", "오일 교체");

        //when
        postService.editPost(member, savedPost.getPostId(), postEditDto);

        //then
        Post editPost = postRepository.findById(savedPost.getPostId()).get();
        assertThat(editPost.getContents()).isEqualTo("수정 후 내용");
    }

    @DisplayName("작성자와 다른 사람이 수정하면 예외가 발생한다.")
    @Test
    void editPostWithOtherMember(){
        //given
        Member member1 = memberRepository.findMemberByLoginId("client1").get();
        Member member2 = memberRepository.findMemberByLoginId("client2").get();
        Region region = regionRepository.findByRegionName("강남구").get();
        Post savedPost = savePost(region, member1,"기스, 깨짐", "오일 교체");
        PostCreateDto postEditDto = new PostCreateDto();

        //when //then
        assertThatThrownBy(() -> postService.editPost(member2, savedPost.getPostId(), postEditDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTHOR_ONLY_ACCESS);
    }

    @DisplayName("게시글 수정 시나리오")
    @TestFactory
    Collection<DynamicTest> editPostDynamicTest() {
        //given
        Member client = memberRepository.findMemberByLoginId("client1").get();
        Member center = memberRepository.findMemberByLoginId("center1").get();
        Region region = regionRepository.findByRegionName("강남구").get();
        Post savedPost = savePost(region, client,"기스, 깨짐", "오일 교체");

        return List.of(
                DynamicTest.dynamicTest("댓글이 달리기 전에는 게시글을 수정할 수 있다.", () -> {
                    //given
                    PostCreateDto postEditDto = createPostDto(LOCATION,"수정 가능", "기스, 깨짐", "오일 교체");

                    //when
                    postService.editPost(client, savedPost.getPostId(), postEditDto);

                    //then
                    Post editPost = postRepository.findById(savedPost.getPostId()).get();
                    assertThat(editPost.getContents()).isEqualTo("수정 가능");
                }),
                DynamicTest.dynamicTest("댓글이 달리면 게시글을 수정할 수 없다.", () -> {
                    //given
                    saveOffer(1, savedPost, center);
                    PostCreateDto postEditDto = createPostDto(LOCATION, "수정 불가능", "기스, 깨짐", "오일 교체");

                    //when //then
                    assertThatThrownBy(() -> postService.editPost(client, savedPost.getPostId(), postEditDto))
                            .isInstanceOf(BusinessException.class)
                            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRE_AUCTION_ONLY);
                })
        );
    }

    @DisplayName("존재하지 않는 게시글을 수정하려고 하면 예외가 발생한다.")
    @Test
    void editPostWithNoExistPos(){
        //given
        Member client = memberRepository.findMemberByLoginId("client1").get();
        Region region = regionRepository.findByRegionName("강남구").get();
        Post savedPost = savePost(region, client, "기스, 깨짐", "오일 교체");

        Long postId = savedPost.getPostId() + 1L;
        PostCreateDto postEditDto = createPostDto(LOCATION, "수정 불가능", "기스, 깨짐", "오일 교체");

        //when //then
        assertThatThrownBy(() -> postService.editPost(client, postId, postEditDto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NOT_FOUND_POST);

    }

    @DisplayName("게시글을 삭제할 수 있다.")
    @Test
    void deletePost(){
        //given
        Member member = memberRepository.findMemberByLoginId("client1").get();
        Region region = regionRepository.findByRegionName("강남구").get();
        Post savedPost = savePost(region, member,"기스, 깨짐", "오일 교체");

        //when
        postService.deletePost(member, savedPost.getPostId());

        //then
        verify(imageService, times(1)).deleteImage(any());
        assertThat(postRepository.findById(savedPost.getPostId())).isEmpty();
    }

    @DisplayName("작성자와 다른 사람이 게시글을 삭제하려고 하면 예외가 발생한다.")
    @Test
    void deletePostWithOtherMember(){
        //given
        Member member1 = memberRepository.findMemberByLoginId("client1").get();
        Member member2 = memberRepository.findMemberByLoginId("client2").get();
        Region region = regionRepository.findByRegionName("강남구").get();

        Post savedPost = savePost(region, member1,"기스, 깨짐", "오일 교체");

        //when //then
        assertThatThrownBy(() -> postService.deletePost(member2, savedPost.getPostId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTHOR_ONLY_ACCESS);
    }

    @DisplayName("경매가 시작된 후 게시글을 삭제하려고 하면 예외가 발생한다.")
    @Test
    void deletePostAfterOffer(){
        //given
        Member client = memberRepository.findMemberByLoginId("client1").get();
        Member center = memberRepository.findMemberByLoginId("center1").get();
        Region region = regionRepository.findByRegionName("강남구").get();

        Post savedPost = savePost(region, client,"기스, 깨짐", "오일 교체");
        saveOffer(1, savedPost, center);

        //when //then
        assertThatThrownBy(() -> postService.deletePost(client, savedPost.getPostId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRE_AUCTION_ONLY);
    }

    @Test
    @DisplayName("게시글 작성자는 조회 시 모든 견적 목록과 함께 조회 가능하다.")
    void showPostWithWriter(){
        // given
        Member writer = memberRepository.findMemberByLoginId("client1").get();
        Member center1 = memberRepository.findMemberByLoginId("center1").get();
        Member center2 = memberRepository.findMemberByLoginId("center2").get();
        Region region = regionRepository.findByRegionName("강남구").get();
        Post savedPost = savePost(region, writer,"기스, 깨짐", "오일 교체");

        saveOffer(10000, savedPost, center1);
        saveOffer(20000, savedPost, center2);

        // when
        Object result = postService.showPost(savedPost.getPostId(), writer);

        // then
        assertThat(result).isInstanceOf(PostWithOffersDto.class);
        PostWithOffersDto postWithOffersDto = (PostWithOffersDto) result;
        assertThat(postWithOffersDto.getOfferDetails()).hasSize(2)
                .extracting("centerName", "price")
                .containsExactly(tuple("center1", 10000),
                        tuple("center2", 20000));
    }

    @DisplayName("경매가 진행중인 게시글을 로그인하지 않은 사용자가 조회 시 예외가 발생한다.")
    @Test
    void showPostWithNotUser() {
        //given
        Region region = regionRepository.findByRegionName("강남구").get();
        Member writer = memberRepository.findMemberByLoginId("client1").get();
        Post savedPost = savePost(region, writer,"기스, 깨짐", "오일 교체");

        //when //then
        assertThatThrownBy(() -> postService.showPost(savedPost.getPostId(), null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", LOGIN_REQUIRED);
    }

    @Test
    @DisplayName("경매 마감된 게시글 조회 요청 시 모든 유저가 모든 댓글 목록과 함께 조회 가능하다.")
    void showPostWithDonePost() {
        // given
        Member writer = memberRepository.findMemberByLoginId("client1").get();
        Region region = regionRepository.findByRegionName("강남구").get();
        Post post = savePost(region, writer, "기스, 깨짐", "오일 교체");
        post.setDone();
        Post savedPost = postRepository.save(post);

        // when
        Object result = postService.showPost(savedPost.getPostId(), null);

        // then
        assertThat(result).isInstanceOf(PostWithOffersDto.class);
    }

    @Test
    @DisplayName("경매에 참여하지 않은 유저가 경매가 진행중인 게시글 조회 요청 시 평균 제시가만 조회 가능하다")
    void showPost_withNotDonePostAndNotParticipant() {
        // given
        Member writer = memberRepository.findMemberByLoginId("client1").get();
        Member client2 = memberRepository.findMemberByLoginId("client2").get();
        Member center1 = memberRepository.findMemberByLoginId("center1").get();
        Member center2 = memberRepository.findMemberByLoginId("center2").get();
        Region region = regionRepository.findByRegionName("강남구").get();
        Post savedPost = savePost(region, writer, "기스, 깨짐", "오일 교체");

        saveOffer(10000, savedPost, center1);
        saveOffer(20000, savedPost, center2);

        // when
        Object result = postService.showPost(savedPost.getPostId(), client2);

        // then
        PostWithAvgPriceDto postWithAvgPrice = (PostWithAvgPriceDto) result;
        assertThat(result).isInstanceOf(PostWithAvgPriceDto.class);
        assertThat(postWithAvgPrice)
                .extracting("avgPrice", "offerDetail")
                .containsExactly(15000.0, null);
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 대해 조회 요청 시 예외가 발생한다.")
    void showPostWithNotFoundPost() {
        // given
        Long unknownPostId = 1000L;

        //when //then
        assertThatThrownBy(() -> postService.showPost(unknownPostId, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NOT_FOUND_POST);
    }

    @Test
    @DisplayName("로그인하지 않은 유저가 경매 중인 게시글 조회 요청 시 예외가 발생한다.")
    void showPostLogInRequired() {
        // given
        Member member1 = memberRepository.findMemberByLoginId("client1").get();
        Region region = regionRepository.findByRegionName("강남구").get();
        Post post = savePost(region, member1, null, null);

        // when
        assertThatThrownBy(() -> postService.showPost(post.getPostId(), null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", LOGIN_REQUIRED);
    }

    @Test
    @DisplayName("유저(클라이언트)가 조건에 따른 게시글 목록을 조회할 수 있다.")
    void showPosts_byNotCenter() {
        // given
        Region region = regionRepository.findByRegionName("강남구").get();
        Member member1 = memberRepository.findMemberByLoginId("client1").get();

        savePost(region, member1, "기스, 깨짐", "오일 교체");
        savePost(region, member1, "판금, 텐트", "타이어 교체");

        // when
        List<PostThumbnailDto> actual = postService.showPosts(true, false, "강남구", "판금", "타이어 교체", member1);

        // then
        assertThat(actual).hasSize(1);
    }

    @Test
    @DisplayName("센터가 다중 조건 선택에 따른 게시글 목록을 조회할 수 있다.")
    void showPosts_byCenter() {
        // given
        Member centerMember = memberRepository.findMemberByLoginId("center1").get();
        Center center = centerRepository.save(createCenter(centerMember));
        Member client = memberRepository.findMemberByLoginId("client1").get();

        Region region = regionRepository.findByRegionName("강남구").get();
        Post post1 = savePost(region, client,"기스, 깨짐", "오일 교체");
        Post post2 = savePost(region, client,"기스, 깨짐", "오일 교체");

        postPerCenterRepository.save(new PostPerCenter(center, post2));

        // when
        List<PostThumbnailDto> actual = postService.showPosts(null, null, null, null, null, centerMember);

        // then
        assertThat(actual).hasSize(1);
    }

    private Post savePost(Region region, Member member, String repairService, String tuneUpService) {
        PostCreateDto postCreateDto = createPostDto(LOCATION, "내용", repairService, tuneUpService);
        Post post = Post.createPost(postCreateDto, region, member);
        return postRepository.save(post);
    }

    private Offer saveOffer(int price, Post post, Member center) {
        Offer offer = Offer.builder()
                .price(price)
                .contents("견적 제시")
                .post(post)
                .center(center).build();

        return offerRepository.save(offer);
    }

    private Center createCenter(Member member) {
        return Center.builder()
                .latitude(0)
                .longitude(0)
                .member(member)
                .build();
    }

    private static PostCreateDto createPostDto(String location, String contents, String repairService, String tuneUpService) {
        return PostCreateDto.builder()
                .carType("승용차")
                .modelName("제네시스")
                .deadline(3)
                .location(location)
                .repairService(repairService)
                .tuneUpService(tuneUpService)
                .centers(new ArrayList<>())
                .contents(contents)
                .build();
    }
}
