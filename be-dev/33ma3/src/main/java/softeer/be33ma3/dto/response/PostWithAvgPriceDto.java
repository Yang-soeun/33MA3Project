package softeer.be33ma3.dto.response;

import lombok.Getter;

@Getter
public class PostWithAvgPriceDto {
    private PostDetailDto postDetail;

    private double avgPrice;

    private OfferDetailDto offerDetail;      // 견적을 작성한 이력이 있는 서비스 센터의 경우 작성한 댓글 정보 보내주기

    public PostWithAvgPriceDto(PostDetailDto postDetailDto, double avgPrice) {
        this.postDetail = postDetailDto;
        this.avgPrice = avgPrice;
    }

    public void setOfferDetailDto(OfferDetailDto offerDetailDto) {
        this.offerDetail = offerDetailDto;
    }
}
