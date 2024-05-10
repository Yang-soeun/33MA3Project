package softeer.be33ma3.dto.response;

import lombok.Builder;
import lombok.Getter;
import softeer.be33ma3.domain.Image;
import softeer.be33ma3.domain.Post;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static softeer.be33ma3.utils.StringParser.stringCommaParsing;
import static softeer.be33ma3.utils.TimeCalculator.calculateDuration;

@Getter
@Builder
public class PostThumbnailDto {
    private Long postId;

    private Long writerId;

    private String modelName;

    private String createTime;

    private int dDay;

    private List<String> imageList;

    private List<String> repairList;

    private List<String> tuneUpList;

    private int offerCount;

    // Post Entity -> PostThumbnailDto 변환
    public static PostThumbnailDto fromEntity(Post post) {
        List<String> imageList = post.getImages().stream().map(Image::getLink).toList();
        Duration duration = calculateDuration(post);
        int dDay = -1;
        if(!post.isDone() && !duration.isNegative())        // 아직 마감 시간 전
            dDay = (int)duration.toDays();

        return PostThumbnailDto.builder()
                .postId(post.getPostId())
                .writerId(post.getMember().getMemberId())
                .modelName(post.getModelName())
                .createTime(createTimeFormatting(post.getCreateTime()))
                .dDay(dDay)
                .repairList(stringCommaParsing(post.getRepairService()))
                .tuneUpList(stringCommaParsing(post.getTuneUpService()))
                .imageList(imageList)
                .offerCount(post.getOffers().size()).build();
    }

    private static String createTimeFormatting(LocalDateTime rawCreateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return rawCreateTime.format(formatter);
    }
}
