package com.jammit_be.storage;

import com.jammit_be.common.properties.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3FileStorage implements FileStorage {
    private final S3Client s3Client;
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    /**
     * MultipartFile을 받아 S3에 저장하고, 접근 가능한 URL을 반환한다.
     * @param file 업로드할 파일 (ex: 프로필 이미지)
     * @param subFolder S3 내 하위 폴더 (ex: "profile" 등, 없으면 root에 저장)
     * @return 저장된 파일의 S3 URL
     */
    @Override
    public String save(MultipartFile file, String subFolder) {

        try{
            // 1. 파일명 및 확장자 추출
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1); // 확장자

            // 2. S3에 저장될 파일명(고유값+확장자) 생성
            String uuid = UUID.randomUUID().toString();
            String key = (subFolder != null && !subFolder.isBlank())
                    ? subFolder + "/" + uuid + "." + ext // ex: profile/uuid.png
                    : uuid + "." + ext;  // ex: uuid.png

            // 3. S3 PutObject 요청 준비
            InputStream inputStream = file.getInputStream(); // 파일의 입력 스트림
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket) // 버킷 이름
                    .key(key) // S3 경로+파일명
                    .contentType(file.getContentType()) // 컨텐츠 타입 (image/png 등)
                    .build();

            // 4. S3 업로드 실행
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));

            // 5. S3 파일 접근 URL 생성 및 반환
            return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;

        } catch (Exception e) {
            throw new RuntimeException("S3 파일 업로드 실패", e);
        }
    }
}
