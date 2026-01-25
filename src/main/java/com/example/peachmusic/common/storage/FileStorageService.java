package com.example.peachmusic.common.storage;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.FileType;
import com.example.peachmusic.common.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final long IMAGE_MAX_SIZE = 5 * 1024 * 1024;
    private static final Set<String> IMAGE_ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");

    private static final long AUDIO_MAX_SIZE = 30 * 1024 * 1024;
    private static final Set<String> AUDIO_ALLOWED_EXT = Set.of("mp3", "wav", "m4a", "flac", "aac", "ogg");

    /**
     * 이미지, 음원 파일을 서버에 저장하고,
     * DB에 저장할 파일 접근 경로를 반환함
     *
     * @param file 업로드된 이미지, 음원 파일
     * @return 저장된 파일의 접근 경로
     */
    public String storeFile(MultipartFile file, FileType type) {
        validateFile(file, type);

        // 원본 파일명에서 확장자 추출
        String ext = getExtension(file.getOriginalFilename());

        if (ext.isBlank()) {
            throw new CustomException(type == FileType.AUDIO ? ErrorCode.AUDIO_INVALID_TYPE : ErrorCode.IMAGE_INVALID_TYPE);
        }

        // 파일명 충돌 방지를 위해 UUID 기반 파일명 생성
        String filename = UUID.randomUUID() + "." + ext;

        // 프로젝트 실행 경로 기준(user.dir)으로 로컬 저장 디렉토리 지정
        Path dir = resolveDir(type);

        try {
            // 디렉토리가 없으면 생성
            Files.createDirectories(dir);

            // 실제 파일 저장 경로 생성
            Path target = dir.resolve(filename);

            // 업로드된 파일을 지정한 경로에 저장
            file.transferTo(target.toFile());

        } catch (IOException e) {
            // 파일 저장중 예외 발생 시
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        // DB에 저장할 파일 접근 경로 반환
        return "/uploads/" + type.folder() + "/" + filename;
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
        if (ext.isBlank() || !IMAGE_ALLOWED_EXT.contains(ext))  {
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

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
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

    private Path resolveDir(FileType type) {
        return Paths.get(System.getProperty("user.dir"), "uploads", type.folder());
    }
}
