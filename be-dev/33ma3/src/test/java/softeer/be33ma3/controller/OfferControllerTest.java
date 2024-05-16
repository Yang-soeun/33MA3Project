package softeer.be33ma3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import softeer.be33ma3.domain.Member;
import softeer.be33ma3.dto.request.OfferCreateDto;
import softeer.be33ma3.jwt.JwtProvider;
import softeer.be33ma3.repository.MemberRepository;
import softeer.be33ma3.service.OfferService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class OfferControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @MockBean private OfferService offerService;
    private static String accessToken;

    @BeforeEach
    void setUp(){
        Member member = Member.createClient("테스트", "1234", null);
        Member savedMember = memberRepository.save(member);
        accessToken = jwtProvider.createAccessToken(savedMember.getMemberType(), savedMember.getMemberId(), savedMember.getLoginId());
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("댓글 세부사항을 반환한다.")
    void showOffer() throws Exception {
        // given // when // then
        mockMvc.perform(get("/post/{post_id}/offer/{offer_id}", 1L, 1L)
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("견적 불러오기 성공"));
    }

    @Test
    @DisplayName("해당하는 게시글에 견적 댓글을 생성한다.")
    void createOffer() throws Exception {
        // given
        OfferCreateDto offerCreateDto = new OfferCreateDto(1, "offer"); // 생성에 필요한 DTO 객체 생성

        // when // then
        mockMvc.perform(post("/post/{post_id}/offer", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offerCreateDto)) // DTO 객체를 JSON 문자열로 변환하여 요청 본문에 추가
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("입찰 성공"))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("견적 제시 가격이 1만원보다 작으면 예외가 발생한다.")
    void createOfferWithUnder1() throws Exception {
        // given
        OfferCreateDto offerCreateDto = new OfferCreateDto(0, "offer");

        // when // then
        mockMvc.perform(post("/post/{post_id}/offer", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offerCreateDto))
                        .header("Authorization", accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("제시 금액은 1만원 이상이어야 합니다."));
    }


    @Test
    @DisplayName("견적 제시 가격이 1000만원보다 크면 예외가 발생한다")
    void createOfferWithOver1000() throws Exception {
        // given
        OfferCreateDto offerCreateDto = new OfferCreateDto(1001, "offer");

        // when // then
        mockMvc.perform(post("/post/{post_id}/offer", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offerCreateDto))
                        .header("Authorization", accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("제시 금액은 1000만원 이하여야 합니다."));
    }

    @Test
    @DisplayName("해당 댓글을 수정할 수 있다.")
    void updateOffer() throws Exception {
        // given
        OfferCreateDto offerCreateDto = new OfferCreateDto(10, "offer"); // 수정에 필요한 DTO 객체 생성

        // when // then
        mockMvc.perform(patch("/post/{post_id}/offer/{offer_id}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offerCreateDto)) // DTO 객체를 JSON 문자열로 변환하여 요청 본문에 추가
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("댓글 수정 성공"))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("해당 댓글을 삭제할 수 있다.")
    void deleteOffer() throws Exception {
        // given // when // then
        mockMvc.perform(delete("/post/{post_id}/offer/{offer_id}", 1L, 1L)
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("댓글 삭제 성공"))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("해당 댓글을 낙찰할 수 있다.")
    void selectOffer() throws Exception {
        // given // when // then
        mockMvc.perform(get("/post/{post_id}/offer/{offer_id}/select", 1L, 1L)
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("낙찰 완료, 게시글 마감"));
    }
}
