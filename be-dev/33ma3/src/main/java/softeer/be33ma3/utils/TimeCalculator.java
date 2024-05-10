package softeer.be33ma3.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import softeer.be33ma3.domain.Post;

public class TimeCalculator {
    public static Duration calculateDuration(Post post) {
        LocalDateTime endTime = post.getCreateTime().plusDays(post.getDeadline());
        endTime = endTime.withHour(23).withMinute(59).withSecond(59);
        return Duration.between(LocalDateTime.now(), endTime);
    }
}
