package softeer.be33ma3.repository.offer;

import static softeer.be33ma3.domain.QOffer.offer;
import static softeer.be33ma3.domain.QReview.review;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import softeer.be33ma3.dto.response.OfferDetailDto;

@Repository
@RequiredArgsConstructor
public class OfferCustomRepositoryImpl implements OfferCustomRepository{
    private final JPAQueryFactory jpaQueryFactory;
    @Override
    public List<OfferDetailDto> findOfferAndAvgPriceByCenterId(List<Long> centerIds) {
        return jpaQueryFactory
                .select(Projections.constructor(OfferDetailDto.class,
                        offer.offerId,
                        offer.center.memberId,
                        offer.center.loginId,
                        offer.price,
                        offer.contents,
                        offer.selected,
                        review.score.avg().doubleValue(),
                        offer.center.image))
                .from(offer).join(review).on(offer.center.memberId.eq(review.center.memberId))
                .where(review.center.memberId.in(centerIds))
                .groupBy(offer.center.memberId)
                .orderBy(offer.price.asc(), review.score.avg().desc())
                .fetch();
    }
}
