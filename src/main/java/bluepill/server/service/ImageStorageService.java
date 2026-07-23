package bluepill.server.service;

import bluepill.server.dto.image.ImagePresignedResponse;
import bluepill.server.dto.image.ImageType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageStorageService {

    private  final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    public ImagePresignedResponse createPresignedUrl(Long userId, String originalFilename, String contentType, ImageType imageType){

        //파일명 생성
        String key = imageType.getPrefix()+ "/" + userId + "/" + UUID.randomUUID() + "_" + originalFilename;

        //요청 객체
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        //10분 유효
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(objectRequest)
                .build();

        //서명 요청
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new ImagePresignedResponse(
                presignedRequest.url().toString(),
                key
        );
    };

    // R2 객체 삭제 (key 가 비어있으면 무시)
    public void deleteImage(String key) {
        if (key == null || key.isBlank()) return;
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }
}
