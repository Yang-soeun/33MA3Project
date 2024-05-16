package softeer.be33ma3.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.io.FileInputStream;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import softeer.be33ma3.domain.Image;

@SpringBootTest
class ImageServiceTest {
    @Autowired private ImageService imageService;
    @MockBean private S3Service s3Service;

    @DisplayName("1개의 이미지를 저장할 수 있다.")
    @Test
    void saveImage() throws IOException {
        //given
        String fileName = "fileName";
        String fileUrl = "fileUrl.png";
        MockMultipartFile image = createImages();

        given(s3Service.uploadFile(any(MultipartFile.class))).willReturn(fileName);
        given(s3Service.getFileUrl(anyString())).willReturn(fileUrl);

        //when
        Image savedImage = imageService.saveImage(image);

        //then
        Assertions.assertThat(savedImage)
                .extracting("link", "fileName")
                .containsExactly("fileUrl.png", "fileName");
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