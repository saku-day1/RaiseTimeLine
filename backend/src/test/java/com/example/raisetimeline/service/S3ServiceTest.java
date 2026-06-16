package com.example.raisetimeline.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    S3Client s3Client;
    @Mock
    S3Presigner s3Presigner;

    S3Service s3Service;

    @BeforeEach
    void setUp() {
        s3Service = new S3Service(s3Client, s3Presigner);
        ReflectionTestUtils.setField(s3Service, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(s3Service, "presignedUrlExpirationSeconds", 3600L);
    }

    // --- uploadImage ---

    @Test
    void uploadImage_validJpegFile_uploadsAndReturnsKey() throws Exception {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg",
                new byte[100]);
        String key = s3Service.uploadImage("profiles", 1L, file);

        assertThat(key).startsWith("profiles/1/");
        assertThat(key).endsWith(".jpg");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadImage_validPngFile_returnsKeyWithPngExtension() throws Exception {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png",
                new byte[100]);
        String key = s3Service.uploadImage("profiles", 1L, file);

        assertThat(key).endsWith(".png");
    }

    @Test
    void uploadImage_validGifFile_returnsKeyWithGifExtension() throws Exception {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        MockMultipartFile file = new MockMultipartFile("file", "anim.gif", "image/gif",
                new byte[100]);
        String key = s3Service.uploadImage("posts", 2L, file);

        assertThat(key).endsWith(".gif");
    }

    @Test
    void uploadImage_emptyFile_throwsIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.jpg", "image/jpeg",
                new byte[0]);

        assertThatThrownBy(() -> s3Service.uploadImage("profiles", 1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("空");
    }

    @Test
    void uploadImage_invalidContentType_throwsIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf",
                new byte[100]);

        assertThatThrownBy(() -> s3Service.uploadImage("profiles", 1L, file))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void uploadImage_exceedsSizeLimit_throwsIllegalArgumentException() {
        byte[] largeData = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile file = new MockMultipartFile("file", "big.jpg", "image/jpeg", largeData);

        assertThatThrownBy(() -> s3Service.uploadImage("profiles", 1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5MB");
    }

    // --- generatePresignedUrl ---

    @Test
    void generatePresignedUrl_callsPresignerAndReturnsUrl() throws Exception {
        PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
        when(presigned.url()).thenReturn(new URL("https://s3.example.com/bucket/key?sig=abc"));
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

        String url = s3Service.generatePresignedUrl("profiles/1/uuid.jpg");

        assertThat(url).startsWith("https://s3.example.com");
        verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }

    // --- resolveUrl ---

    @Test
    void resolveUrl_nullInput_returnsNull() {
        assertThat(s3Service.resolveUrl(null)).isNull();
    }

    @Test
    void resolveUrl_httpsUrl_returnsAsIs() {
        String url = "https://cdn.example.com/image.jpg";
        assertThat(s3Service.resolveUrl(url)).isEqualTo(url);
        verifyNoInteractions(s3Presigner);
    }

    @Test
    void resolveUrl_httpUrl_returnsAsIs() {
        String url = "http://cdn.example.com/image.jpg";
        assertThat(s3Service.resolveUrl(url)).isEqualTo(url);
        verifyNoInteractions(s3Presigner);
    }

    @Test
    void resolveUrl_s3Key_callsGeneratePresignedUrl() throws Exception {
        PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
        when(presigned.url()).thenReturn(new URL("https://s3.example.com/presigned"));
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

        String result = s3Service.resolveUrl("profiles/1/uuid.jpg");

        assertThat(result).isEqualTo("https://s3.example.com/presigned");
        verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }
}
