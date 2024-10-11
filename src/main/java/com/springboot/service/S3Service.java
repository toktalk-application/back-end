package com.springboot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName;

    public S3Service(@Value("${aws.s3.bucket-name}") String bucketName,
                     @Value("${aws.access-key-id}") String accessKeyId,
                     @Value("${aws.secret-access-key}") String secretAccessKey,
                     @Value("${aws.region}") String region) {
        this.bucketName = bucketName;
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }

    // MultipartFile을 처리하여 S3에 업로드하는 메서드 (파일 유형에 따라 자동으로 폴더 지정)
    public String uploadFile(MultipartFile multipartFile) {
        String folderName = getFolderNameByFileType(multipartFile);  // 파일 유형에 따라 폴더 경로 설정
        String fileName = folderName + multipartFile.getOriginalFilename();

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.putObject(putObjectRequest,
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                            multipartFile.getInputStream(), multipartFile.getSize()));

            return getFileUrl(fileName);
        } catch (S3Exception | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 파일 유형에 따라 업로드 경로 설정 (이미지/녹음 파일 등)
    private String getFolderNameByFileType(MultipartFile multipartFile) {
        String contentType = multipartFile.getContentType();

        if (contentType == null) {
            throw new IllegalArgumentException("파일의 MIME 타입을 확인할 수 없습니다.");
        }
        // 이미지 파일인지 확인
        if (contentType.startsWith("image")) {
            return "image/";
        }
        // 오디오 파일인지 확인
        else if (contentType.startsWith("audio")) {
            return "recording/";
        }
        else {
            throw new IllegalArgumentException("지원되지 않는 파일 유형입니다: " + contentType);
        }
    }

    public String getFileUrl(String fileName) {
        return "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
    }
}
