package softeer.be33ma3.dto.response;

import jakarta.validation.constraints.NegativeOrZero.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
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

//    // 제시 가격 저렴한 순 -> 별점 높은 순 정렬
//    @Override
//    public int compareTo(OfferDetailDto other) {
//        int priceDifference = Integer.compare(this.price, other.price);
//        if (priceDifference != 0) { //가격이 다른 경우
//            return priceDifference; //가격 기준
//        }
//
//        return Double.compare(other.score, this.score); //가격이 같은 경우 별점 기준
//    }
}
