package com.mcp.mcp_pilot.document.adapter.out;

import com.mcp.mcp_pilot.common.config.S3Config;
import com.mcp.mcp_pilot.common.exception.ErrorCode;
import com.mcp.mcp_pilot.document.exception.FileException;
import com.mcp.mcp_pilot.document.port.in.dto.UploadFileCommand;
import com.mcp.mcp_pilot.document.port.out.FileStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class R2StorageAdapter implements FileStoragePort {

    private final S3Client s3Client;
    private final S3Config s3Config;

    @Override
    public String upload(UploadFileCommand file) {
        String key = generateKey(file.fileName());

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Config.bucketName())
                .key(key)
                .contentType(file.contentType())
                .contentLength(file.fileSize())
                .build();

        try (InputStream inputStream = file.inputStream()){
            byte[] bytes = inputStream.readAllBytes();
            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(bytes)
            );

            log.info(
                    "[R2StorageAdapter] 파일 업로드 완료. fileName={}, key={}",
                    file.fileName(),
                    key
            );
            return key;
        } catch (S3Exception e) {
            log.error(
                    "[R2StorageAdapter] 파일 업로드 실패. fileName={}, key={}",
                    file.fileName(),
                    key,
                    e
            );
            throw new FileException(ErrorCode.FILE_UPLOAD);
        } catch (IOException e) {
            log.error("[R2StorageAdapter] 파일 스트림 읽기 실패. fileName={}", file.fileName(), e);
            throw new FileException(ErrorCode.FILE_UPLOAD);
        }
    }

    @Override
    public InputStream download(String r2Key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3Config.bucketName())
                .key(r2Key)
                .build();

        try {
            return s3Client.getObject(
                    request,
                    ResponseTransformer.toInputStream()
            );
        } catch (S3Exception e) {
            log.error(
                    "[R2StorageAdapter] 파일 다운로드 실패. key={}",
                    r2Key,
                    e
            );

            throw new FileException(ErrorCode.FILE_DOWNLOAD);
        }
    }

    @Override
    public void delete(String r2Key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(s3Config.bucketName())
                .key(r2Key)
                .build();

        try {
            s3Client.deleteObject(request);
            log.info("[R2StorageAdapter] R2 파일 삭제 완료. key={}", r2Key);
        } catch (S3Exception e) {
            log.error("[R2StorageAdapter] R2 파일 삭제 실패. key={}", r2Key, e);
            throw new FileException(ErrorCode.FILE_DELETE);
        }
    }

    private String generateKey(String fileName) {
        return "documents/%s/%s".formatted(UUID.randomUUID(), fileName);
    }
}
