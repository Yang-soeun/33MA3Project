package softeer.be33ma3.repository.offer;

import static softeer.be33ma3.domain.QOffer.offer;
import static softeer.be33ma3.domain.QReview.review;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
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

        NumberExpression<Double> avgScore = Expressions.numberTemplate(Double.class, "coalesce({0}, 0.0)", review.score.avg().doubleValue());

        return jpaQueryFactory
                .select(Projections.constructor(OfferDetailDto.class,
                        offer.offerId,
                        offer.center.memberId,
                        offer.center.loginId,
                        offer.price,
                        offer.contents,
                        offer.selected,
                        avgScore,
                        offer.center.image))
                .from(offer).leftJoin(review).on(offer.center.memberId.eq(review.center.memberId))
                .where(offer.center.memberId.in(centerIds))
                .groupBy(offer.center.memberId)
                .orderBy(offer.price.asc(), review.score.avg().desc())
                .fetch();
    }
}
