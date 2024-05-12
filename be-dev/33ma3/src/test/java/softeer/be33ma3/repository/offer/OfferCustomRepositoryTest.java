package softeer.be33ma3.repository.offer;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import softeer.be33ma3.domain.Member;
import softeer.be33ma3.domain.Offer;
import softeer.be33ma3.domain.Post;
import softeer.be33ma3.domain.Region;
import softeer.be33ma3.domain.Review;
import softeer.be33ma3.dto.request.PostCreateDto;
import softeer.be33ma3.dto.response.OfferDetailDto;
import softeer.be33ma3.repository.MemberRepository;
import softeer.be33ma3.repository.RegionRepository;
import softeer.be33ma3.repository.post.PostRepository;
import softeer.be33ma3.repository.review.ReviewRepository;

@ActiveProfiles("test")
@SpringBootTest
class OfferCustomRepositoryTest {
    @Autowired private MemberRepository memberRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private OfferRepository offerRepository;
    @Autowired private RegionRepository regionRepository;
    @Autowired private ReviewRepository reviewRepository;

    @BeforeEach
    void setUp(){
        Member center1 = Member.createCenter("center1", "1234", "test.png");
        Member client1 = Member.createClient("client1", "1234", "test.png");
        Member savedCenter1 = memberRepository.save(center1);
        Member savedClient = memberRepository.save(client1);

        //게시글 생성
        PostCreateDto postCreateDto = createPostDto();
        Region region = regionRepository.save(new Region(1L, "강남구"));
        Post post = Post.createPost(postCreateDto, region, savedClient);
        Post savedPost = postRepository.save(post);
        savedPost.setDone();

        //리뷰 작성
        saveReview(savedClient, savedCenter1, savedPost);
    }

    @AfterEach
    void tearDown() {
        reviewRepository.deleteAllInBatch();
        offerRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        regionRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("견적 정보와 견적을 작성한 센터의 평균을 알 수 있다. - 가격이 동일한 경우 별점 순으로 내림차순 정렬")
    @Test
    void findOfferAndAvgPriceByCenterIdWithSamePrice(){
        //given
        Member center1 = memberRepository.findMemberByLoginId("center1").get();
        Member client1 = memberRepository.findMemberByLoginId("client1").get();
        Member center2 = Member.createCenter("center2", "1234", "test.png");
        Member savedCenter2 = memberRepository.save(center2);

        //게시글 생성
        PostCreateDto postCreateDto = createPostDto();
        Region region = regionRepository.save(new Region(1L, "강남구"));
        Post post = Post.createPost(postCreateDto, region, client1);
        Post savedPost = postRepository.save(post);

        //견적 생성
        saveOffer(savedPost, savedCenter2, 1000);
        saveOffer(savedPost, center1, 1000);//별점이 더 높음

        //when
        List<OfferDetailDto> offerDetailDtos = offerRepository.findOfferAndAvgPriceByCenterId(
                List.of(center1.getMemberId(), savedCenter2.getMemberId()));

        //then
        assertThat(offerDetailDtos).hasSize(2)
                .extracting("centerName", "score", "price")
                .containsExactly(tuple("center1", 4.3, 1000),
                        tuple("center2", 0.0, 1000));
    }

    @DisplayName("견적 정보와 견적을 작성한 센터의 평균을 알 수 있다. - 가격이 다른 경우 오름차순 정렬")
    @Test
    void findOfferAndAvgPriceByCenterIdWithDiffPrice(){
        //given
        Member center1 = memberRepository.findMemberByLoginId("center1").get();
        Member client1 = memberRepository.findMemberByLoginId("client1").get();
        Member center2 = Member.createCenter("center2", "1234", "test.png");
        Member savedCenter2 = memberRepository.save(center2);

        //게시글 생성
        PostCreateDto postCreateDto = createPostDto();
        Region region = regionRepository.save(new Region(1L, "강남구"));
        Post post = Post.createPost(postCreateDto, region, client1);
        Post savedPost = postRepository.save(post);

        //견적 생성
        saveOffer(savedPost, savedCenter2, 1000);//가격이 더 저렴함
        saveOffer(savedPost, center1, 100000);//별점이 더 높음

        //when
        List<OfferDetailDto> offerDetailDtos = offerRepository.findOfferAndAvgPriceByCenterId(
                List.of(center1.getMemberId(), savedCenter2.getMemberId()));

        //then
        assertThat(offerDetailDtos).hasSize(2)
                .extracting("centerName", "score", "price")
                .containsExactly(tuple("center2", 0.0, 1000),
                        tuple("center1", 4.3, 100000));
    }

    private static PostCreateDto createPostDto() {
        return PostCreateDto.builder()
                .carType("승용차")
                .modelName("제네시스")
                .deadline(3)
                .location("서울시 강남구")
                .repairService("기스, 깨짐")
                .tuneUpService("오일 교체")
                .centers(new ArrayList<>())
                .contents("내용")
                .build();
    }

    private void saveReview(Member savedClient, Member savedCenter1, Post savedPost) {
        Review review = Review.builder()
                .score(4.3)
                .writer(savedClient)
                .center(savedCenter1)
                .contents("좋아요")
                .post(savedPost)
                .build();

        reviewRepository.save(review);
    }

    private void saveOffer(Post post, Member center, int price) {
        Offer offer = Offer.builder()
                .price(price)
                .contents("견적 제시")
                .post(post)
                .center(center).build();

        offerRepository.save(offer);
    }
}