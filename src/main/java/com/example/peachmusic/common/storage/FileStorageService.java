package com.example.peachmusic.common.storage;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.FileType;
import com.example.peachmusic.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

@Service
public class FileStorageService {

    private static final long IMAGE_MAX_SIZE = 5 * 1024 * 1024;
    private static final Set<String> IMAGE_ALLOWED_EXT = Set.of("jpg", "jpeg", "png");

    private static final long AUDIO_MAX_SIZE = 30 * 1024 * 1024;
    private static final Set<String> AUDIO_ALLOWED_EXT = Set.of("mp3", "wav");

    private final S3Client s3Client;
    private final String assetsBucket;
    private final String mediaBucket;
    private final String assetsPublicBase;

    public FileStorageService(S3Client s3Client,
                              @Value("${r2.bucket.assets}") String assetsBucket,
                              @Value("${r2.bucket.media}") String mediaBucket,
                              @Value("${r2.public-base}") String assetsPublicBase
    ) {
        this.s3Client = s3Client;
        this.assetsBucket = assetsBucket;
        this.mediaBucket = mediaBucket;
        this.assetsPublicBase = assetsPublicBase;
    }

    /**
     * 이미지, 음원 파일을 서버에 저장하고,
     * DB에 저장할 파일 접근 경로를 반환함
     *
     * @param file 업로드된 이미지, 음원 파일
     * @return 저장된 파일의 접근 경로
     */
    public String storeFile(MultipartFile file, FileType type, String baseName) {
        validateFile(file, type);

        // 원본 파일명에서 확장자 추출
        String ext = getExtension(file.getOriginalFilename());

        if (ext.isBlank()) {
            throw new CustomException(type == FileType.AUDIO ? ErrorCode.AUDIO_INVALID_TYPE : ErrorCode.IMAGE_INVALID_TYPE);
        }

        // baseName 정리(특수문자 제거/공백 정리/길이 제한)
        String safeBaseName = sanitizeBaseName(baseName);

        if (safeBaseName.isBlank()) {
            // 이름이 전부 특수문자였다거나 하면 빈 문자열이 될 수 있음
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        String filename = safeBaseName + "." + ext;

        String key = buildKey(type, filename);

        try {
            byte[] bytes = file.getBytes();

            String bucket = (type == FileType.AUDIO) ? mediaBucket : assetsBucket;

            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength((long) bytes.length)
                    .build(), RequestBody.fromBytes(bytes)
            );

        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        if (type == FileType.AUDIO) {
            return key;
        } else {
            return assetsPublicBase.replaceAll("/+$", "") + "/" + key;
        }
    }

    private String buildKey(FileType fileType, String fileName) {
        if (fileType == FileType.AUDIO) {
            return "storage/audio/" + fileName;
        }
        return "storage/image/" + fileType.folder() + "/" + fileName;
    }

    private void validateFile(MultipartFile file, FileType type) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_REQUIRED);
        }

        if (type == FileType.AUDIO) {
            validateAudio(file);
        } else {
            validateImage(file);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file.getSize() > IMAGE_MAX_SIZE) {
            throw new CustomException(ErrorCode.IMAGE_TOO_LARGE);
        }

        // 파일 확장자 추출
        String ext = getExtension(file.getOriginalFilename());

        // 확장자가 비어 있거나, 허용된 목록에 없을 경우
        if (ext.isBlank() || !IMAGE_ALLOWED_EXT.contains(ext)) {
            throw new CustomException(ErrorCode.IMAGE_INVALID_TYPE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(ErrorCode.IMAGE_INVALID_TYPE);
        }
    }

    private void validateAudio(MultipartFile file) {
        if (file.getSize() > AUDIO_MAX_SIZE) {
            throw new CustomException(ErrorCode.AUDIO_TOO_LARGE);
        }

        String ext = getExtension(file.getOriginalFilename());

        if (ext.isBlank() || !AUDIO_ALLOWED_EXT.contains(ext)) {
            throw new CustomException(ErrorCode.AUDIO_INVALID_TYPE);
        }
    }

    /**
     * 파일명에서 확장자를 추출함
     *
     * @param filename 원본 파일명
     * @return 소문자 확장자 (확장자가 없으면 빈 문자열)
     */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        // 마지막 '.' 이후 문자열을 확장자로 사용
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private String sanitizeBaseName(String input) {
        if (input == null) {
            return "";
        }

        String s = input.trim().toLowerCase();

        // 공백 전부 제거
        s = s.replaceAll("\\s+", "");

        // 특수문자 제거
        s = s.replaceAll("[\\\\/:*?\"<>|._-]", "");

        // 너무 길면 자르기(서버 안전)
        int max = 80;
        if (s.length() > max) {
            s = s.substring(0, max);
        }

        return s;
    }

    public void deleteFileByPath(String path) {

        if (path == null || path.isBlank()) {
            return;
        }

        String key = extractKey(path);

        boolean isAudio = key.startsWith("storage/audio/") || key.startsWith("storage/streaming/");
        String bucket = isAudio ? mediaBucket : assetsBucket;

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (Exception exception) {
            throw new CustomException(ErrorCode.FILE_DELETED_FAILED);
        }
    }

    public String extractKey(String pathOrUrl) {
        String trimmedPathOrUrl = pathOrUrl.trim();

        if (trimmedPathOrUrl.startsWith("/uploads/")) {
            throw new CustomException(ErrorCode.FILE_DELETED_FAILED);
        }

        if (trimmedPathOrUrl.startsWith("http://") || trimmedPathOrUrl.startsWith("https://")) {
            URI uri = URI.create(trimmedPathOrUrl);
            String path = uri.getPath();
            return path.startsWith("/") ? path.substring(1) : path;
        }

        return trimmedPathOrUrl.startsWith("/") ? trimmedPathOrUrl.substring(1) : trimmedPathOrUrl;
    }
}
