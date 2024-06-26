package softeer.be33ma3.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import softeer.be33ma3.domain.Member;
import softeer.be33ma3.domain.Offer;
import softeer.be33ma3.domain.Post;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfferCreateDto {
    @Min(value = 1, message = "제시 금액은 1만원 이상이어야 합니다.")
    @Max(value = 1000, message = "제시 금액은 1000만원 이하여야 합니다.")
    private int price;

    private String contents;

    public Offer toEntity(Post post, Member center) {
        return Offer.builder()
                .price(price)
                .contents(contents)
                .post(post)
                .center(center).build();
    }
}

