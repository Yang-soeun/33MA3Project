package softeer.be33ma3.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import softeer.be33ma3.domain.Image;
import softeer.be33ma3.repository.ImageRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ImageServiceTest {
    private final static String FILE_NAME = "fileName";
    private final static String FILE_URL = "fileUrl.png";

    @Autowired private ImageService imageService;
    @Autowired private ImageRepository imageRepository;
    @MockBean
    private S3Service s3Service;

    @DisplayName("1개의 이미지를 저장할 수 있다.")
    @Test
    void saveImage() throws IOException {
        //given
        MockMultipartFile image = createImages();

        given(s3Service.uploadFile(any(MultipartFile.class))).willReturn(FILE_NAME);
        given(s3Service.getFileUrl(anyString())).willReturn(FILE_URL);

        //when
        Image savedImage = imageService.saveImage(image);

        //then
        assertThat(savedImage)
                .extracting("link", "fileName")
                .containsExactly("fileUrl.png", "fileName");
    }

    @DisplayName("여러개의 이미지를 저장할 수 있다.")
    @Test
    void saveImages() throws IOException {
        //given
        List<MultipartFile> multipartFiles = new ArrayList<>();
        multipartFiles.add(createImages());
        multipartFiles.add(createImages());

        given(s3Service.uploadFile(any(MultipartFile.class))).willReturn(FILE_NAME);
        given(s3Service.getFileUrl(anyString())).willReturn(FILE_URL);

        //when
        List<Image> images = imageService.saveImages(multipartFiles);

        //then
        verify(s3Service, times(2)).uploadFile(any(MultipartFile.class));
        verify(s3Service, times(2)).getFileUrl(anyString());
        assertThat(images).hasSize(2);
    }

    @DisplayName("이미지를 삭제할 수 있다.")
    @Test
    void deleteImage(){
        //given
        Image image1 = Image.createImage("link1", "fileName1");
        Image image2 = Image.createImage("link2", "fileName2");
        Image savedImage1 = imageRepository.save(image1);
        Image savedImage2 = imageRepository.save(image2);

        //when
        imageService.deleteImage(List.of(savedImage1, savedImage2));

        //then
        List<Image> result = imageRepository.findAllById(List.of(savedImage1.getImageId(), savedImage2.getImageId()));
        assertThat(result).isEmpty();
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