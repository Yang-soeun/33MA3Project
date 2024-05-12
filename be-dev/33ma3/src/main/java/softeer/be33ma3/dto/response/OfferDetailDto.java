package softeer.be33ma3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import softeer.be33ma3.domain.Offer;

@Data
@Builder
@AllArgsConstructor
public class OfferDetailDto {
    private Long offerId;

    private Long memberId;

    private String centerName;

    private int price;

    private String contents;

    private boolean selected;

    private Double score;

    private String profile;

    public static OfferDetailDto fromEntity(Offer offer, Double score) {
        return OfferDetailDto.builder()
                .offerId(offer.getOfferId())
                .memberId(offer.getCenter().getMemberId())
                .centerName(offer.getCenter().getLoginId())
                .price(offer.getPrice())
                .contents(offer.getContents())
                .selected(offer.isSelected())
                .score(score)
                .profile(offer.getCenter().getImage()).build();
    }
}
