package com.dokdok.global.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.internal-endpoint}")
    private String internalEndpoint;

    @Value("${minio.external-endpoint}")
    private String externalEndpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    /**
     * 내부 통신용 MinioClient (업로드, 버킷 관리 등)
     */
    @Bean
    public MinioClient internalMinioClient() {
        return MinioClient.builder()
                .endpoint(internalEndpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * Presigned URL 생성용 MinioClient (외부 URL 기준 서명 생성)
     */
    @Bean
    public MinioClient externalMinioClient() {
        return MinioClient.builder()
                .endpoint(externalEndpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

}
