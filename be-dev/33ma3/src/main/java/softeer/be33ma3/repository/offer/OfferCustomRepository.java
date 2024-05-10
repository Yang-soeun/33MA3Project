package softeer.be33ma3.repository.offer;

import java.util.List;
import softeer.be33ma3.dto.response.OfferDetailDto;

public interface OfferCustomRepository{
    List<OfferDetailDto> findOfferAndAvgPriceByCenterId(List<Long> centerIds);
}
