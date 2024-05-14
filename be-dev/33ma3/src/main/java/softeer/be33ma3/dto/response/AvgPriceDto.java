package softeer.be33ma3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class AvgPriceDto {
    private double avgPrice;
    public static AvgPriceDto from(double avgPrice){
        return new AvgPriceDto(Math.round( avgPrice * 10 ) / 10.0);
    }
}
