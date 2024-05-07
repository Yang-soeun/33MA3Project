package softeer.be33ma3.dto.response;

import lombok.Builder;
import lombok.Getter;
import softeer.be33ma3.domain.Image;
import softeer.be33ma3.domain.Post;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static softeer.be33ma3.utils.StringParser.stringCommaParsing;

@Builder
@Getter
public class PostDetailDto {
    private Long postId;

    private Long writerId;

    private String carType;

    private String modelName;

    private int dDay;

    private int remainTime;

    private String regionName;

    private String contents;

    private List<String> repairList;

    private List<String> tuneUpList;

    private List<String> imageList;

    // Post Entity -> PostDetailDto 변환
    public static PostDetailDto fromEntity(Post post) {
        List<String> imageList = post.getImages().stream().map(Image::getLink).toList();
        Duration duration = calculateDuration(post);
        int dDay = -1;
        int remainTime = 0;
        if(!post.isDone() && !duration.isNegative())        // 아직 마감 시간 전
            dDay = (int)duration.toDays();
        if(dDay == 0)
            remainTime = calculateRemainTime(duration);

        return PostDetailDto.builder()
                .postId(post.getPostId())
                .writerId(post.getMember().getMemberId())
                .carType(post.getCarType())
                .modelName(post.getModelName())
                .dDay(dDay)
                .remainTime(remainTime)
                .regionName(post.getRegion().getRegionName())
                .contents(post.getContents())
                .repairList(stringCommaParsing(post.getRepairService()))
                .tuneUpList(stringCommaParsing(post.getTuneUpService()))
                .imageList(imageList).build();
    }

    public static Duration calculateDuration(Post post) {
        LocalDateTime endTime = post.getCreateTime().plusDays(post.getDeadline());
        endTime = endTime.withHour(23).withMinute(59).withSecond(59);
        return Duration.between(LocalDateTime.now(), endTime);
    }

    // 마감 시간까지 남은 시간을 초 단위로 반환
    public static int calculateRemainTime(Duration duration) {
        return (int) duration.toSeconds();
    }
}
