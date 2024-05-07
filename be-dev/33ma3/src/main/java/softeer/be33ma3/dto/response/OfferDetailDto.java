package softeer.be33ma3.dto.response;

import lombok.Builder;
import lombok.Getter;
import softeer.be33ma3.domain.Offer;

@Getter
@Builder
public class OfferDetailDto implements Comparable<OfferDetailDto> {
    private Long offerId;

    private Long memberId;

    private String centerName;

    private int price;

    private String contents;

    private boolean selected;

    private Double score;

    private String profile;

    // Offer Entity -> OfferDetailDto 변환
    public static OfferDetailDto fromEntity(Offer offer, Double score) {
        return OfferDetailDto.builder()
                .offerId(offer.getOfferId())
                .memberId(offer.getCenter().getMemberId())
                .centerName(offer.getCenter().getLoginId())
                .price(offer.getPrice())
                .contents(offer.getContents())
                .selected(offer.isSelected())
                .score(score)
                .profile(offer.getCenter().getImage().getLink()).build();
    }

    // 제시 가격 저렴한 순 -> 별점 높은 순 정렬
    @Override
    public int compareTo(OfferDetailDto other) {
        if(price != other.price)
            return price - other.price;
        if(score > other.getScore())
            return -1;
        else if(score < other.getScore())
            return 1;
        return 0;
    }
}
