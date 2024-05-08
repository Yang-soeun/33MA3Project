package softeer.be33ma3.docs;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class) //restDoscs에 대한 확장 주입
public abstract class RestDocsSupport {
    protected MockMvc mockMvc; //mockMvc를 이용해서 작성하기 위해
    protected ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach //스프링 의존성 없이 mockMvc로만 하는 방식
    void setUp(RestDocumentationContextProvider provider){ //rest docs를 작성하기 위한 mockMvc 설정
        this.mockMvc = MockMvcBuilders.standaloneSetup(initController()) //문서를 만들고 싶은 컨트롤러 넣으면 됨 -> 하위 컨트롤러에서 주입하는 추상 클래스 생성하여 넣기
                .apply(documentationConfiguration(provider))
                .build();
    }

    protected  abstract Object initController();
}