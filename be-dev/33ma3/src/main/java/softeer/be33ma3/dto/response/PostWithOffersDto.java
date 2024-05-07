package softeer.be33ma3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PostWithOffersDto {
    private PostDetailDto postDetail;

    private List<OfferDetailDto> offerDetails;
}
