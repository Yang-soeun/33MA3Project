package softeer.be33ma3.domain;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import softeer.be33ma3.domain.calcuator.DistanceCalculator;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class CenterTest {
    @Autowired
    DistanceCalculator distanceCalculator;

    //given
    private static Stream<Arguments> provideMemberPosForCheckingInRadius(){
        return Stream.of(
                Arguments.of(37.509, 127.0, 1.0, true), //0.9차이가 1km차이
                Arguments.of(36.5, 126.0, 1.0, false));
    }

    @DisplayName("반경 안 거리이면 true, 아니면 false를 반환한다.")
    @MethodSource("provideMemberPosForCheckingInRadius")
    @ParameterizedTest
    void isWithinRadius(double memberLatitude, double memberLongitude, double radius, boolean expected){
        //given
        Center center = Center.builder()
                .latitude(37.5)
                .longitude(127.0)
                .build();

        //when
        boolean withinRadius = center.isWithinRadius(distanceCalculator, memberLatitude, memberLongitude, radius);

        //then
        assertThat(withinRadius).isEqualTo(expected);
    }
}
