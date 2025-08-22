package com.studyapp.controller;

import com.studyapp.entity.FileAttachment;
import com.studyapp.entity.StudyGroup;
import com.studyapp.entity.User;
import com.studyapp.repository.FileAttachmentRepository;
import com.studyapp.repository.StudyGroupRepository;
import com.studyapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private FileAttachmentRepository fileAttachmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("studyGroupId") Long studyGroupId,
            @RequestParam("userId") Long userId) {

        try {
            if (file.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "File is empty");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Optional<User> userOpt = userRepository.findById(userId);
            Optional<StudyGroup> studyGroupOpt = studyGroupRepository.findById(studyGroupId);

            if (!userOpt.isPresent() || !studyGroupOpt.isPresent()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User or study group not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 파일 저장 디렉토리 생성
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 고유한 파일명 생성
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // 파일 저장
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath);

            // 데이터베이스에 파일 정보 저장
            FileAttachment fileAttachment = new FileAttachment();
            fileAttachment.setStudyGroup(studyGroupOpt.get());
            fileAttachment.setUploader(userOpt.get());
            fileAttachment.setFileName(originalFileName);
            fileAttachment.setFileUrl("/api/files/download/" + uniqueFileName);
            fileAttachment.setFileType(file.getContentType());
            fileAttachment.setFileSize(file.getSize());
            fileAttachment.setUploadedAt(LocalDateTime.now());

            fileAttachmentRepository.save(fileAttachment);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File uploaded successfully");
            response.put("fileName", originalFileName);
            response.put("fileUrl", fileAttachment.getFileUrl());
            response.put("fileId", fileAttachment.getId());
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/{studyGroupId}")
    public ResponseEntity<List<Map<String, Object>>> getFiles(@PathVariable Long studyGroupId) {
        try {
            List<FileAttachment> files = fileAttachmentRepository.findByStudyGroupIdOrderByUploadedAtDesc(studyGroupId);

            List<Map<String, Object>> response = files.stream().map(file -> {
                Map<String, Object> fileMap = new HashMap<>();
                fileMap.put("id", file.getId());
                fileMap.put("studyGroupId", file.getStudyGroup().getId());
                fileMap.put("fileName", file.getFileName());
                fileMap.put("fileUrl", file.getFileUrl());
                fileMap.put("fileType", file.getFileType());
                fileMap.put("fileSize", file.getFileSize());
                fileMap.put("uploadedAt", file.getUploadedAt().toString());

                // Uploader 정보
                Map<String, Object> uploaderMap = new HashMap<>();
                uploaderMap.put("id", file.getUploader().getId());
                uploaderMap.put("name", file.getUploader().getName());
                uploaderMap.put("email", file.getUploader().getEmail());
                uploaderMap.put("createdAt", file.getUploader().getCreatedAt().toString());
                fileMap.put("uploader", uploaderMap);

                return fileMap;
            }).toList();


            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileBytes = Files.readAllBytes(filePath);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .body(fileBytes);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(
            @PathVariable Long fileId,
            @RequestParam Long userId) {

        try {
            Optional<FileAttachment> fileOpt = fileAttachmentRepository.findById(fileId);

            if (!fileOpt.isPresent()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "File not found");
                return ResponseEntity.notFound().build();
            }

            FileAttachment fileAttachment = fileOpt.get();

            // 파일 업로더만 삭제 가능
            if (!fileAttachment.getUploader().getId().equals(userId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Only the uploader can delete this file");
                return ResponseEntity.status(403).body(errorResponse);
            }

            // 실제 파일 삭제
            String fileName = fileAttachment.getFileUrl().substring(fileAttachment.getFileUrl().lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Files.deleteIfExists(filePath);

            // 데이터베이스에서 삭제
            fileAttachmentRepository.delete(fileAttachment);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}