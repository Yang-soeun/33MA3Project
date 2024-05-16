package softeer.be33ma3.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import softeer.be33ma3.domain.Member;
import softeer.be33ma3.domain.Offer;
import softeer.be33ma3.domain.Post;
import softeer.be33ma3.dto.request.PostCreateDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import softeer.be33ma3.repository.offer.OfferRepository;
import softeer.be33ma3.repository.post.PostRepository;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class OfferRepositoryTest {

    @Autowired
    private OfferRepository offerRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void tearDown() {
        offerRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("게시글 아이디를 이용하여 해당 게시글의 모든 견적 댓글 목록을 가져올 수 있다.")
    void findByPost_PostId() {
        // given
        Post savedPost = savePost(null);
        saveOffer(1, "offer1", savedPost, null);
        saveOffer(2, "offer2", savedPost, null);
        saveOffer(3, "offer3", savedPost, null);

        // when
        List<Offer> offerList = offerRepository.findByPost_PostId(savedPost.getPostId());
        // then
        assertThat(offerList).hasSize(3)
                .extracting("contents")
                .containsExactly("offer1", "offer2", "offer3");
    }

    @Test
    @DisplayName("견적 댓글이 하나도 달리지 않은 게시글의 견적 댓글 목록을 가져올 경우 빈 배열이 반환된다.")
    void findByPost_PostId_WithNoOffer() {
        // given
        Post savedPost = savePost(null);
        // when
        List<Offer> offers = offerRepository.findByPost_PostId(savedPost.getPostId());
        // then
        assertThat(offers).hasSize(0);
    }

    @Test
    @DisplayName("게시글에서 해당하는 서비스 센터가 작성한 견적 댓글을 반환할 수 있다.")
    void findByPost_PostIdAndCenter_CenterId() {
        // given
        Post savedPost = savePost(null);
        Member savedCenter = saveCenterMember("center1", "1234");
        saveOffer(10, "offer1", savedPost, savedCenter);
        // when
        Offer offer = offerRepository.findByPost_PostIdAndCenter_MemberId(savedPost.getPostId(),
                savedCenter.getMemberId()).get();
        // then
        assertThat(offer)
                .extracting("price", "contents")
                .containsExactly(10, "offer1");
    }

    @Test
    @DisplayName("게시글에 해당 센터가 견적 댓글을 작성한 이력이 없을 경우 Optional.empty()로 반환된다.")
    void findByPost_PostIdAndCenter_CenterId_WithNoOffer() {
        // given
        Post savedPost = savePost(null);
        Member savedCenter = saveCenterMember("center1", "1234");

        // when
        Optional<Offer> result = offerRepository.findByPost_PostIdAndCenter_MemberId(savedPost.getPostId(), savedCenter.getMemberId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("post id와 offer id를 이용하여 해당 게시글에 달린 해당하는 견적 댓글을 하나 반환한다.")
    void findByPost_PostIdAndOfferId() {
        // given
        Post savedPost = savePost(null);
        Offer savedOffer = saveOffer(10, "offer1", savedPost, null);

        // when
        Offer offer = offerRepository.findByPost_PostIdAndOfferId(savedPost.getPostId(), savedOffer.getOfferId()).get();
        // then
        assertThat(offer).extracting("price", "contents")
                .containsExactly(10, "offer1");
    }

    @Test
    @DisplayName("post id, offer id에 해당하는 견적 댓글이 없을 경우 Optional.empty()로 반환된다.")
    void findByPost_PostIdAndOfferId_WithNoOffer() {
        // given
        Post savedPost = savePost(null);
        Long notExistOfferId = 99L;
        // when
        Optional<Offer> result = offerRepository.findByPost_PostIdAndOfferId(savedPost.getPostId(), notExistOfferId);
        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("게시글에서 낙찰된 센터를 찾을 수 있다.")
    void findSelectedCenterByPostId() {
        // given
        Post savedPost = savePost(null);
        Member savedCenter1 = saveCenterMember( "center1", "1234");
        Offer savedOffer = saveOffer(10, "offer1", savedPost, savedCenter1);
        savedOffer.setSelected();
        offerRepository.save(savedOffer);

        // when
        Member member = offerRepository.findSelectedCenterByPostId(savedPost.getPostId()).get();

        // then
        assertThat(member).extracting("loginId").isEqualTo("center1");
    }

    @Test
    @DisplayName("해당 게시글에서 낙찰된 센터를 찾고 싶을 때 낙찰된 센터가 없을 경우 Optional.empty()가 반환된다.")
    void findSelectedCenterByPostId_WithNoSelect() {
        // given
        Post savedPost = savePost(null);
        Member center1 = saveCenterMember("center1", "center1");
        Member center2 = saveCenterMember("center2", "center2");
        saveOffer(1, "offer1", savedPost, center1);
        saveOffer(2, "offer2", savedPost, center2);

        // when
        Optional<Member> result = offerRepository.findSelectedCenterByPostId(savedPost.getPostId());
        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("해당 게시글에 달린 견적 댓글의 평균 제시 가격을 계산하여 반환한다.")
    void findAvgPriceByPostId() {
        // given
        Post savedPost = savePost(null);
        // offer 저장하기
        Member center1 = saveCenterMember( "center1", "1234");
        Member center2 = saveCenterMember( "center2", "1234");
        Member center3 = saveCenterMember( "center3", "1234");
        Member center4 = saveCenterMember( "center4", "1234");
        saveOffer(1, "offer1", savedPost, center1);
        saveOffer(2, "offer2", savedPost, center2);
        saveOffer(2, "offer3", savedPost, center3);
        saveOffer(2, "offer4", savedPost, center4);
        // when
        Double avgPrice = offerRepository.findAvgPriceByPostId(savedPost.getPostId()).get();

        // then
        assertThat(avgPrice).isEqualTo(1.75);
    }

    @Test
    @DisplayName("해당 게시글에 견적 댓글이 달리지 않았을 경우 평균 제시 가격은 Optional.empty()로 반환된다.")
    void findAvgPriceByPostId_WithNoOffer() {
        // given
        Post savedPost = savePost(null);
        // when
        Optional<Double> result = offerRepository.findAvgPriceByPostId(savedPost.getPostId());
        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("게시글에 견적 댓글을 제시한 모든 센터의 멤버 아이디를 가져온다.")
    void findCenterMemberIdsByPost_PostId() {
        // given
        Post savedPost = savePost(null);
        Member center1 = saveCenterMember("center1", "1234");
        Member center2 = saveCenterMember( "center2", "1234");
        Member center3 = saveCenterMember("center3", "1234");
        Member center4 = saveCenterMember( "center4", "1234");
        saveOffer(1, "offer1", savedPost, center1);
        saveOffer(2, "offer2", savedPost, center2);
        saveOffer(2, "offer3", savedPost, center3);
        saveOffer(2, "offer4", savedPost, center4);

        // when
        List<Long> memberIds = offerRepository.findCenterMemberIdsByPost_PostId(savedPost.getPostId());

        // then
        List<Long> expected = List.of(center1.getMemberId(), center2.getMemberId(), center3.getMemberId(), center4.getMemberId());
        assertThat(memberIds).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("게시글에 견적 댓글을 제시한 모든 센터의 멤버 아이디를 가져올 때, 견적 댓글이 없을 경우 빈 배열이 반환된다.")
    void findCenterMemberIdsByPost_PostId_WithNoOffer() {
        // given
        Post savedPost = savePost(null);
        // when
        List<Long> result = offerRepository.findCenterMemberIdsByPost_PostId(savedPost.getPostId());
        // then
        assertThat(result).hasSize(0);
    }

    private Offer saveOffer(int price, String contents, Post post, Member center) {
        Offer offer = Offer.builder()
                .price(price)
                .contents(contents)
                .post(post)
                .center(center).build();
        return offerRepository.save(offer);
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

    private Member saveCenterMember(String loginId, String password){
        Member center = Member.createCenter(loginId, password, null);
        return memberRepository.save(center);
    }
}
