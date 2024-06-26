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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

import softeer.be33ma3.domain.Member;
import softeer.be33ma3.dto.request.PostCreateDto;
import softeer.be33ma3.jwt.JwtProvider;
import softeer.be33ma3.repository.MemberRepository;
import softeer.be33ma3.service.ImageService;
import softeer.be33ma3.service.PostService;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostControllerTest {
    private static String accessToken;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private MemberRepository memberRepository;
    @MockBean private PostService postService;
    @MockBean private ImageService imageService;

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

    @DisplayName("게시글을 생성할 수 있다.")
    @Test
    void createPost() throws Exception {
        //given
        PostCreateDto postCreateDto = PostCreateDto.builder()
                .carType("승용차")
                .modelName("제네시스")
                .deadline(3)
                .location("서울시 강남구")
                .repairService("기스, 깨짐")
                .tuneUpService("오일 교체")
                .centers(new ArrayList<>())
                .contents("게시글 생성")
                .build();

        MockMultipartFile multipartFile = createImages();
        String request = objectMapper.writeValueAsString(postCreateDto);

        //when //then
        mockMvc.perform(multipart("/post/create")
                        .file(new MockMultipartFile("request", "", "application/json", request.getBytes(StandardCharsets.UTF_8)))
                        .file(multipartFile)
                        .contentType(MULTIPART_FORM_DATA)
                        .accept(APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("게시글 작성 성공"))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @DisplayName("게시글 작성 시 차종을 입력하지 않으면 예외가 발생한다.")
    @Test
    void createPostWithBlankField() throws Exception {
        //given
        PostCreateDto postCreateDto = PostCreateDto.builder()
                .modelName("제네시스")
                .deadline(3)
                .location("서울시 강남구")
                .repairService("기스, 깨짐")
                .tuneUpService("오일 교체")
                .centers(new ArrayList<>())
                .contents("게시글 생성")
                .build();

        String request = objectMapper.writeValueAsString(postCreateDto);

        //when //then
        mockMvc.perform(multipart("/post/create")
                        .file(new MockMultipartFile("request", "", "application/json", request.getBytes(StandardCharsets.UTF_8)))
                        .contentType(MULTIPART_FORM_DATA)
                        .accept(APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("차종은 필수입니다."));
    }

    @DisplayName("게시글 작성시 마감기한이 10이 넘으면 예외가 발생한다.")
    @Test
    void createPostWithOverDeadLine() throws Exception {
        //given
        PostCreateDto postCreateDto = PostCreateDto.builder()
                .carType("승용차")
                .modelName("제네시스")
                .deadline(11)
                .location("서울시 강남구")
                .repairService("기스, 깨짐")
                .tuneUpService("오일 교체")
                .centers(new ArrayList<>())
                .contents("게시글 생성")
                .build();

        String request = objectMapper.writeValueAsString(postCreateDto);

        //when //then
        mockMvc.perform(multipart("/post/create")
                        .file(new MockMultipartFile("request", "", "application/json", request.getBytes(StandardCharsets.UTF_8)))
                        .contentType(MULTIPART_FORM_DATA)
                        .accept(APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("최대 10일까지 가능합니다."));
    }


    @DisplayName("게시글을 수정할 수 있다.")
    @Test
    void editPost() throws Exception {
        //given
        PostCreateDto request = PostCreateDto.builder()
                .carType("승용차")
                .modelName("제네시스")
                .deadline(3)
                .location("서울시 강남구")
                .repairService("기스, 깨짐")
                .tuneUpService("오일 교체")
                .centers(new ArrayList<>())
                .contents("게시글 수정")
                .build();

        //when //then
        mockMvc.perform(put("/post/{post_id}", 1)
                        .header("Authorization", accessToken)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("게시글 수정 성공"));
    }

    @DisplayName("게시글을 삭제할 수 있다.")
    @Test
    void deletePost() throws Exception {
        //given //when //then
        mockMvc.perform(delete("/post/{post_id}", 1)
                        .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("게시글 삭제 성공"));
    }

    @Test
    @DisplayName("특정 게시글을 조회할 수 있다.")
    void showPost() throws Exception {
        // given // when // then
        mockMvc.perform(get("/post/one/{post_id}", 1L)
                .header("Authorization", accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("게시글 조회 완료"));
    }

    @Test
    @DisplayName("필터링을 통한 게시글 조회를 할 수 있다.")
    void showPosts() throws Exception {
        // given
        boolean mine = true;
        boolean done = true;
        String region = "강남구, 양천구";
        String repair = "판금, 덴트";
        String tuneUp = "엔진 오일, 타이어 교체";

        mockMvc.perform(get("/post?mine={mine}&done={done}&region={region}&repair={repair}&tuneUp={tuneUp}", mine, done, region, repair, tuneUp))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andDo(print())
                .andExpect(jsonPath("$.message").value("게시글 목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray());
    }

    private MockMultipartFile createImages() throws IOException {
        String fileName = "testImage"; //파일명
        String contentType = "jpg"; //파일타입
        String filePath = "src/test/resources/testImage/"+fileName+"."+contentType;

        FileInputStream fileInputStream = new FileInputStream(filePath);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "images",
                fileName + "." + contentType,
                contentType,
                fileInputStream
        );
        return multipartFile;
    }
}
