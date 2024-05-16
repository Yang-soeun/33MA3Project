package softeer.be33ma3.service;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import softeer.be33ma3.domain.Member;
import softeer.be33ma3.domain.Offer;
import softeer.be33ma3.domain.Post;
import softeer.be33ma3.dto.request.OfferCreateDto;
import softeer.be33ma3.dto.request.PostCreateDto;
import softeer.be33ma3.dto.response.OfferDetailDto;
import softeer.be33ma3.exception.BusinessException;
import softeer.be33ma3.repository.MemberRepository;
import softeer.be33ma3.repository.offer.OfferRepository;
import softeer.be33ma3.repository.post.PostRepository;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static softeer.be33ma3.exception.ErrorCode.ALREADY_SUBMITTED;
import static softeer.be33ma3.exception.ErrorCode.AUTHOR_ONLY_ACCESS;
import static softeer.be33ma3.exception.ErrorCode.CLOSED_POST;
import static softeer.be33ma3.exception.ErrorCode.NOT_CENTER;
import static softeer.be33ma3.exception.ErrorCode.NOT_FOUND_OFFER;
import static softeer.be33ma3.exception.ErrorCode.NOT_FOUND_POST;
import static softeer.be33ma3.exception.ErrorCode.ONLY_LOWER_AMOUNT_ALLOWED;

@SpringBootTest
@ActiveProfiles("test")
class OfferServiceTest {
    private final String DEFAULT_PROFILE  = "default_profile.png";

    @Autowired private OfferRepository offerRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private OfferService offerService;

    @AfterEach
    void tearDown() {
        offerRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("견적 댓글 하나를 반환할 수 있다.")
    void showOffer() {
        // given
        Member writer = saveClient("writer", "1234");
        Post post = savePost(writer);
        Member center1 = saveCenter("center1", "1234");
        Member center2 = saveCenter("cetner2", "1234");
        Offer offer1 = saveOffer(10, "offer1", post, center1);
        Offer offer2 = saveOffer(100, "offer2", post, center2);

        // when
        OfferDetailDto offerDetailDto = offerService.showOffer(post.getPostId(), offer1.getOfferId());

        // then
        assertThat(offerDetailDto).extracting("centerName", "price")
                .containsExactly("center1", 10);
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 대해 견적 댓글 조회 요청 시 예외가 발생한다.")
    void showOfferWithNotExistPost() {
        // given
        Member writer = saveClient("writer", "1234");
        Post post = savePost(writer);
        Member center = saveCenter("center1", "center1");
        Offer offer = saveOffer(10, "offer1", post, center);

        Long notExistPostId = 1000L;

        //when //then
        assertThatThrownBy(() -> offerService.showOffer(notExistPostId, offer.getOfferId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NOT_FOUND_POST);
    }

    @Test
    @DisplayName("존재하지 않는 견적 댓글에 대해 조회 요청 시 예외가 발생한다.")
    void showOfferWithNotExistOffer() {
        // given
        Member writer = saveClient("writer", "user1");
        Post post = savePost(writer);
        Long notExistOfferId = 1000L;

        //when //them
        assertThatThrownBy(() -> offerService.showOffer(post.getPostId(), notExistOfferId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NOT_FOUND_OFFER);
    }

    @Test
    @DisplayName("견적 댓글을 작성할 수 있다.")
    void createOffer() {
        // given
        Member writer = saveClient("writer", "1234");
        Post post = savePost(writer);
        Member center = saveCenter("center1", "1234");
        OfferCreateDto offerCreateDto = new OfferCreateDto(10, "create offer");

        // when
        Long offerId = offerService.createOffer(post.getPostId(), offerCreateDto, center);

        // then
        Offer offer = offerRepository.findByPost_PostIdAndOfferId(post.getPostId(), offerId).get();
        assertThat(offer).extracting("price", "contents")
                .containsExactly(10, "create offer");
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 대해 견적 댓글 작성 시 예외가 발생한다.")
    void createOfferWithNotExistPost() {
        // given
        Member center = saveCenter("center1", "center1");
        OfferCreateDto offerCreateDto = new OfferCreateDto(10, "create offer");
        Long notExistPostId = 1000L;

        // when //then
        assertThatThrownBy(() -> offerService.createOffer(notExistPostId, offerCreateDto, center))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NOT_FOUND_POST);
    }

    @Test
    @DisplayName("이미 마감된 게시글에 대해 견적 댓글 작성 요청 시 예외가 발생한다.")
    void createOfferWithAlreadyDonPost() {
        // given
        Member writer = saveClient("writer", "1234");
        PostCreateDto postCreateDto = PostCreateDto.builder()
                .carType("승용차")
                .modelName("제네시스")
                .deadline(0)
                .location("서울시 강남구")
                .repairService("기스, 깨짐")
                .tuneUpService("오일 교체, 타이어 교체")
                .centers(new ArrayList<>())
                .contents("게시글 내용")
                .build();

        Post post = Post.createPost(postCreateDto, null, writer);
        post.setDone();
        Post savedPost = postRepository.save(post);
        Member center = saveCenter("center1", "1234");
        OfferCreateDto offerCreateDto = new OfferCreateDto(10, "create offer");

        //when //then
        assertThatThrownBy(() -> offerService.createOffer(savedPost.getPostId(), offerCreateDto, center))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CLOSED_POST);
    }

    @Test
    @DisplayName("센터가 아닌 일반 유저가 견적 댓글 작성 요청 시 예외가 발생한다.")
    void createOfferWithNotCenter() {
        // given
        Member writer = saveClient("writer", "1234");
        Post post = savePost(writer);
        Member client = saveClient("client2", "1234");
        OfferCreateDto offerCreateDto = new OfferCreateDto(10, "create offer");

        // when //then
        assertThatThrownBy(() -> offerService.createOffer(post.getPostId(), offerCreateDto, client))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NOT_CENTER);
    }

    @Test
    @DisplayName("이미 견적 댓글을 작성한 이력이 있는 센터가 견적 댓글 작성 요청 시 예외가 발생한다.")
    void createOfferWithAlreadyWroteOffer() {
        // given
        Member writer = saveClient("writer", "1234");
        Post post = savePost(writer);
        Member center = saveCenter("center1", "1234");
        saveOffer(10, "create offer1", post, center);
        OfferCreateDto offerCreateDto = new OfferCreateDto(9, "create offer2");

        // when //then
        assertThatThrownBy(() -> offerService.createOffer(post.getPostId(), offerCreateDto, center))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ALREADY_SUBMITTED);
    }

    @Test
    @DisplayName("존재하지 않는 견적 댓글에 대해 수정 시 예외가 발생한다.")
    void updateOfferWithNotExistOffer() {
        // given
        Member member = saveClient("writer", "1234");
        Post post = savePost(member);
        Long notExistOfferId = 1000L;

        // when //then
        assertThatThrownBy(() -> offerService.updateOffer(post.getPostId(), notExistOfferId, null, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NOT_FOUND_OFFER);
    }

    @Test
    @DisplayName("견적 댓글 작성자가 아닌 유저가 견적 댓글 수정 요청 시 예외가 발생한다.")
    void updateOfferWithNotWriter() {
        // given
        Member writer = saveClient("writer", "1234");
        Post post = savePost(writer);
        Member center = saveCenter("center1", "1234");
        Offer offer = saveOffer(10, "offer1", post, center);
        Member center2 = saveCenter("center2", "center2");

        // when //then
        assertThatThrownBy(() -> offerService.updateOffer(post.getPostId(), offer.getOfferId(), null, center2))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AUTHOR_ONLY_ACCESS);
    }

    @DisplayName("견적 댓글 수정 시나리오")
    @TestFactory
    Collection<DynamicTest> test(){
        //given
        Member writer = saveClient("writer", "1234");
        Post post = savePost(writer);
        Member center = saveCenter("center1", "1234");
        Offer offer = saveOffer(100, "offer1", post, center);

        return List.of(
                DynamicTest.dynamicTest("기존 제시 가격 보다 높은 가격으로 수정하면 예외가 발생한다.", () -> {
                    //given
                    OfferCreateDto offerCreateDtoWithOverPrice = new OfferCreateDto(150, "update offer");

                    //when //then
                    assertThatThrownBy(() -> offerService.updateOffer(post.getPostId(), offer.getOfferId(),
                            offerCreateDtoWithOverPrice, center))
                            .isInstanceOf(BusinessException.class)
                            .hasFieldOrPropertyWithValue("errorCode", ONLY_LOWER_AMOUNT_ALLOWED);
                }),
                DynamicTest.dynamicTest("기존 제시 가격 보다 낮은 가격으로 수정할 수 있디.", () -> {
                    //given
                    OfferCreateDto offerCreateDtoWithUnderPrice = new OfferCreateDto(50, "success update offer");

                    //when
                    offerService.updateOffer(post.getPostId(), offer.getOfferId(), offerCreateDtoWithUnderPrice, center);

                    //then
                    Offer updatedOffer = offerRepository.findByPost_PostIdAndOfferId(post.getPostId(), offer.getOfferId()).get();
                    assertThat(updatedOffer).extracting("price", "contents")
                            .containsExactly(50, "success update offer");
                }));
    }


    @Test
    @DisplayName("견적 댓글을 삭제할 수 있다.")
    void deleteOffer() {
        // given
        Member member = saveClient("user1", "1234");
        Post post = savePost(member);
        Member center = saveCenter("center1", "1234");
        Offer offer = saveOffer(10, "offer1", post, center);

        // when
        offerService.deleteOffer(post.getPostId(), offer.getOfferId(), center);

        // then
        Optional<Offer> result = offerRepository.findByPost_PostIdAndOfferId(post.getPostId(), offer.getOfferId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("견적 댓글 작성자가 아닌 유저가 견적 댓글 삭제시 예외가 발생한다.")
    void deleteOfferWithNotWriter() {
        // given
        Member member = saveClient("writer", "1234");
        Post post = savePost(member);
        Member center = saveCenter("center1", "1234");
        Offer offer = saveOffer(10, "offer1", post, center);
        Member center2 = saveCenter("center2", "1234");

        // when //then
        assertThatThrownBy(() -> offerService.deleteOffer(post.getPostId(), offer.getOfferId(), center2))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AUTHOR_ONLY_ACCESS);
    }

    @Test
    @DisplayName("제시된 견적 댓글 한개를 낙찰할 수 있다.")
    void selectOffer() {
        // given
        Member member = saveClient("writer", "1234");
        Post post = savePost(member);
        Member center = saveCenter("center1", "1234");
        Offer offer = saveOffer(10, "offer1", post, center);

        // when
        offerService.selectOffer(post.getPostId(), offer.getOfferId(), member);

        // then
        Post actualPost = postRepository.findById(post.getPostId()).get();
        Offer actualOffer = offerRepository.findByPost_PostIdAndOfferId(post.getPostId(), offer.getOfferId()).get();
        assertTrue(actualPost.isDone());
        assertTrue(actualOffer.isSelected());
    }

    @Test
    @DisplayName("게시글 작성자가 아닌 유저가 견적 댓글 낙찰 요청 시 예외가 발생한다.")
    void selectOfferWithNotWriter() {
        // given
        Member writer = saveClient("writer", "1234");
        Post post = savePost(writer);
        Member center = saveCenter("center1", "1234");
        Offer offer = saveOffer(10, "offer1", post, center);
        Member member2 = saveClient("user2", "1234");

        // when //then
        assertThatThrownBy(() -> offerService.selectOffer(post.getPostId(), offer.getOfferId(), member2))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AUTHOR_ONLY_ACCESS);
    }

    @Test
    @DisplayName("존재하지 않는 견적 댓글을 낙찰 요청 시 예외가 발생한다.")
    void selectOfferWithNoOffer() {
        // given
        Member writer = saveClient("writer", "1234");
        Post post = savePost(writer);
        Long notExistOfferId = 1000L;

        // when //then
        assertThatThrownBy(() -> offerService.showOffer(post.getPostId(), notExistOfferId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", NOT_FOUND_OFFER);
    }

    private Post savePost(Member member) {
        PostCreateDto postCreateDto = PostCreateDto.builder()
                .carType("승용차")
                .modelName("제네시스")
                .deadline(0)
                .location("서울시 강남구")
                .repairService("기스, 깨짐")
                .tuneUpService("오일 교체, 타이어 교체")
                .centers(new ArrayList<>())
                .contents("게시글 내용")
                .build();

        return postRepository.save(Post.createPost(postCreateDto, null, member));
    }

    private Offer saveOffer(int price, String contents, Post post, Member center) {
        Offer offer = Offer.builder()
                .price(price)
                .contents(contents)
                .post(post)
                .center(center).build();

        return offerRepository.save(offer);
    }

    private Member saveCenter(String loginId, String password) {
        Member member = Member.createCenter(loginId, password, DEFAULT_PROFILE);
        return memberRepository.save(member);
    }

    private Member saveClient(String loginId, String password) {
        Member member = Member.createClient(loginId, password, DEFAULT_PROFILE);
        return memberRepository.save(member);
    }
}
