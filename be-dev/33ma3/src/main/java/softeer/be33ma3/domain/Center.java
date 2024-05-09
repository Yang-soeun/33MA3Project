package softeer.be33ma3.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import softeer.be33ma3.domain.calcuator.DistanceCalculator;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Center {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long centerId;

    private double latitude;

    private double longitude;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public Center (double latitude, double longitude, Member member){
        this.latitude = latitude;
        this.longitude = longitude;
        this.member = member;
    }

    //반경안에 있는 센터인지 확인하는 메소드
    public boolean isWithinRadius(DistanceCalculator distanceCalculator, double memberLatitude, double memberLongitude, double radius) {
        double distance = distanceCalculator.calculate(memberLatitude, memberLongitude, latitude, longitude);
        return distance <= radius;
    }
}
