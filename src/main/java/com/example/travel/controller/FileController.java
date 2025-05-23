package com.example.travel.controller;

import com.example.travel.common.Result;
import com.example.travel.dto.FileUploadResponse; // 新增 DTO
import com.example.travel.exception.BusinessValidationException; // 自定义异常
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils; // Spring's StringUtils
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder; // For creating download URI

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 允许的图片文件类型
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    // 允许的最大文件大小 (例如 5MB) - Spring Boot 也会有全局配置
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()") // 只有登录用户才能上传
    public ResponseEntity<Result<FileUploadResponse>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessValidationException("File to upload cannot be empty.");
        }

        // 文件大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new MaxUploadSizeExceededException(MAX_FILE_SIZE); // Spring MVC can handle this exception globally
        }

        // 文件类型校验 (示例：只允许图片)
        String contentType = file.getContentType();
        if (!isSupportedContentType(contentType)) {
            throw new BusinessValidationException("Unsupported file type: " + contentType +
                    ". Allowed types are: " + String.join(", ", ALLOWED_IMAGE_TYPES));
        }

        // 清理并生成文件名
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot > 0) {
            fileExtension = originalFilename.substring(lastDot); // .jpg, .png
        }
        // 生成唯一文件名 (保留原始扩展名，如果存在)
        String storedFilename = UUID.randomUUID().toString() + fileExtension;

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }

            Path destinationFile = uploadPath.resolve(storedFilename).normalize().toAbsolutePath();

            // 防止目录遍历攻击 (虽然resolve.normalize应该处理，但多一层检查无妨)
            if (!destinationFile.getParent().equals(uploadPath.toAbsolutePath())) {
                throw new BusinessValidationException("Cannot store file outside of the configured upload directory.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // 构建文件访问URL (假设 /uploads/** 路径已配置为静态资源服务)
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/") // 这个路径需要与你的静态资源配置匹配
                    .path(storedFilename)
                    .toUriString();

            logger.info("File uploaded successfully: {}. Access URI: {}", storedFilename, fileDownloadUri);

            FileUploadResponse response = new FileUploadResponse(storedFilename, fileDownloadUri, file.getContentType(), file.getSize());
            return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(response));

        } catch (IOException ex) {
            logger.error("Could not store file {}. Error: {}", originalFilename, ex.getMessage());
            throw new RuntimeException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }

    private boolean isSupportedContentType(String contentType) {
        return contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase());
    }

    // 可以添加一个下载端点，如果文件不能通过静态资源直接访问
    // 例如: @GetMapping("/download/{filename:.+}")
    // public ResponseEntity<Resource> downloadFile(@PathVariable String filename, HttpServletRequest request) { ... }
}