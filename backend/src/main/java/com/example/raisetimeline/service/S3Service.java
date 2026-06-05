package com.example.raisetimeline.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.presigned-url-expiration:3600}")
    private long presignedUrlExpirationSeconds;

    public String uploadImage(String keyPrefix, Long userId, MultipartFile file) throws IOException {
        validateImage(file);

        String ext = resolveExtension(file.getContentType());
        String key = keyPrefix + "/" + userId + "/" + UUID.randomUUID() + "." + ext;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return key;
    }

    public String generatePresignedUrl(String key) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignedUrlExpirationSeconds))
                .getObjectRequest(r -> r.bucket(bucketName).key(key))
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    public String resolveUrl(String keyOrUrl) {
        if (keyOrUrl == null) return null;
        if (keyOrUrl.startsWith("http://") || keyOrUrl.startsWith("https://")) {
            return keyOrUrl;
        }
        return generatePresignedUrl(keyOrUrl);
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("ファイルが空です");
        }
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") &&
                 !contentType.equals("image/png") &&
                 !contentType.equals("image/gif"))) {
            throw new IllegalArgumentException("JPEG / PNG / GIF のみアップロードできます");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("ファイルサイズは 5MB 以下にしてください");
        }
    }

    private String resolveExtension(String contentType) {
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            default -> "jpg";
        };
    }
}
