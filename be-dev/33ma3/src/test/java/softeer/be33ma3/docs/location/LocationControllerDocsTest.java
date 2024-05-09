package softeer.be33ma3.docs.location;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import softeer.be33ma3.controller.LocationController;
import softeer.be33ma3.docs.RestDocsSupport;
import softeer.be33ma3.dto.response.CenterListDto;
import softeer.be33ma3.service.LocationService;

public class LocationControllerDocsTest extends RestDocsSupport {
    private final LocationService locationService = mock(LocationService.class);
    @Override
    protected Object initController() {
        return new LocationController(locationService);
    }

    @DisplayName("모든 센터 정보를 내려주는 API")
    @Test
    void getAllCenters() throws Exception {
        //given
        CenterListDto center1 = new CenterListDto(1L, 35.4, 127.0);
        CenterListDto center2 = new CenterListDto(2L, 35.4, 127.0);
        List<CenterListDto> result =  List.of(center1, center2);
        given(locationService.getAllCenters()).willReturn(result);

        //when //then
        mockMvc.perform(get("/center/all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("모든 센터 정보 전송 완료"))
                .andExpect(jsonPath("$.data").isArray())
                .andDo(document("all-centers",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("status").type(STRING).description("상태"),
                                fieldWithPath("message").type(STRING).description("성공 메세지"),
                                fieldWithPath("data").type(ARRAY).description("데이터"),
                                fieldWithPath("data[].centerId").type(NUMBER).description("센터 아이디"),
                                fieldWithPath("data[].latitude").type(NUMBER).description("위도"),
                                fieldWithPath("data[].longitude").type(NUMBER).description("경도")
                        )));
    }

    @DisplayName("반경 안 센터 정보를 내려주는 API")
    @Test
    void getCentersInRadius() throws Exception {
        //given
        CenterListDto center1 = new CenterListDto(1L, 35.4, 127.0);
        List<CenterListDto> result =  List.of(center1);
        given(locationService.getAllCenters()).willReturn(result);
        given(locationService.getCentersInRadius(anyDouble(), anyDouble(), anyDouble())).willReturn(result);

        //when //then
        mockMvc.perform(get("/location?latitude=" + anyDouble() + "&longitude=" + anyDouble() + "&radius=" + anyDouble()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("반경 내 위치한 센터 정보 전송 완료"))
                .andExpect(jsonPath("$.data").isArray())
                .andDo(document("inRadius-centers",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(parameterWithName("latitude").description("위도"),
                                parameterWithName("longitude").description("경도"),
                                parameterWithName("radius").description("반지름")),
                        responseFields(
                                fieldWithPath("status").type(STRING).description("상태"),
                                fieldWithPath("message").type(STRING).description("성공 메세지"),
                                fieldWithPath("data").type(ARRAY).description("데이터"),
                                fieldWithPath("data[].centerId").type(NUMBER).description("센터 아이디"),
                                fieldWithPath("data[].latitude").type(NUMBER).description("위도"),
                                fieldWithPath("data[].longitude").type(NUMBER).description("경도")
                        )));;
    }
}
