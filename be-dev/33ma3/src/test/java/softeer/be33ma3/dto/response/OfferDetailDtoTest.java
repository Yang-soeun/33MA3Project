package softeer.be33ma3.dto.response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import softeer.be33ma3.domain.*;
import softeer.be33ma3.repository.ImageRepository;
import softeer.be33ma3.repository.MemberRepository;
import softeer.be33ma3.repository.offer.OfferRepository;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class OfferDetailDtoTest {

    @Autowired private OfferRepository offerRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ImageRepository imageRepository;

    @AfterEach
    void tearDown() {
        offerRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        imageRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Offer Entity와 해당 견적을 작성한 센터의 정보를 넘겨받고 OfferDetailDto로 변환하여 반환한다.")
    void fromEntity() {
        // given
        Member savedMember = createCenter("center1", "center1");
        Offer savedOffer = saveOffer(10, "offer1", null, savedMember);
        OfferDetailDto expected = OfferDetailDto.builder()
                .offerId(savedOffer.getOfferId())
                .memberId(savedOffer.getCenter().getMemberId())
                .price(10)
                .contents("offer1")
                .centerName("center1")
                .profile("profile.png")
                .selected(false)
                .score(4.5).build();
        // when
        OfferDetailDto actual = OfferDetailDto.fromEntity(savedOffer, 4.5);
        // then
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    private Offer saveOffer(int price, String contents, Post post, Member center) {
        Offer offer = Offer.builder()
                .price(price)
                .contents(contents)
                .post(post)
                .center(center).build();
        return offerRepository.save(offer);
    }
    private Member createCenter(String loginId, String password) {
        Image profile = Image.createImage("profile.png", "profile.png");
        String savedProfile = imageRepository.save(profile).getLink();
        Member center = Member.createCenter(loginId, password, savedProfile);
        return memberRepository.save(center);
    }
}
