// src/main/java/com/studyapp/controller/ChatController.java
package com.studyapp.controller;

import java.util.stream.Collectors;
import com.studyapp.entity.ChatMessage;
import com.studyapp.entity.StudyGroup;
import com.studyapp.entity.User;
import com.studyapp.repository.ChatMessageRepository;
import com.studyapp.repository.StudyGroupRepository;
import com.studyapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class ChatController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    // OPTIONS 메서드 명시적 처리
    @RequestMapping(value = "/send", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                .build();
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== 메시지 전송 요청 받음 ===");
            System.out.println("Request data: " + request);

            Long studyGroupId = Long.valueOf(request.get("studyGroupId").toString());
            Long userId = Long.valueOf(request.get("userId").toString());
            String content = (String) request.get("content");
            String messageType = (String) request.getOrDefault("messageType", "text");
            String fileName = (String) request.get("fileName");
            String fileUrl = (String) request.get("fileUrl");

            System.out.println("StudyGroupId: " + studyGroupId + ", UserId: " + userId);

            Optional<User> userOpt = userRepository.findById(userId);
            Optional<StudyGroup> studyGroupOpt = studyGroupRepository.findById(studyGroupId);

            if (!userOpt.isPresent() || !studyGroupOpt.isPresent()) {
                System.out.println("User 또는 StudyGroup을 찾을 수 없음");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User or study group not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            ChatMessage message = new ChatMessage();
            message.setStudyGroup(studyGroupOpt.get());
            message.setSender(userOpt.get());
            message.setContent(content);
            message.setMessageType(messageType);
            message.setFileName(fileName);
            message.setFileUrl(fileUrl);
            message.setCreatedAt(LocalDateTime.now());
            message.setDeleted(false);

            ChatMessage savedMessage = chatMessageRepository.save(message);
            System.out.println("메시지 저장 성공: " + savedMessage.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Message sent successfully");
            response.put("messageId", savedMessage.getId());

            return ResponseEntity.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                    .body(response);

        } catch (Exception e) {
            System.err.println("메시지 전송 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to send message: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .header("Access-Control-Allow-Origin", "*")
                    .body(errorResponse);
        }
    }

    @GetMapping("/{studyGroupId}/messages")
    public ResponseEntity<List<Map<String, Object>>> getMessages(
            @PathVariable Long studyGroupId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) Long lastMessageId) {

        try {
            PageRequest pageRequest = PageRequest.of(0, limit, Sort.by("createdAt").descending());
            List<ChatMessage> messages;

            if (lastMessageId != null) {
                messages = chatMessageRepository.findByStudyGroupIdAndIdLessThanAndIsDeletedFalse(
                        studyGroupId, lastMessageId, pageRequest);
            } else {
                messages = chatMessageRepository.findByStudyGroupIdAndIsDeletedFalse(
                        studyGroupId, pageRequest);
            }

            List<Map<String, Object>> response = messages.stream().map(message -> {
                Map<String, Object> messageMap = new HashMap<>();
                messageMap.put("id", message.getId());
                messageMap.put("studyGroupId", message.getStudyGroup().getId());
                messageMap.put("content", message.getContent());
                messageMap.put("messageType", message.getMessageType());
                messageMap.put("fileName", message.getFileName());
                messageMap.put("fileUrl", message.getFileUrl());
                messageMap.put("createdAt", message.getCreatedAt().toString());
                messageMap.put("isDeleted", message.isDeleted());

                // Sender 정보
                Map<String, Object> senderMap = new HashMap<>();
                senderMap.put("id", message.getSender().getId());
                senderMap.put("name", message.getSender().getName());
                senderMap.put("email", message.getSender().getEmail());
                senderMap.put("createdAt", message.getSender().getCreatedAt().toString());
                messageMap.put("sender", senderMap);

                return messageMap;
            }).collect(Collectors.toList());

            return ResponseEntity.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .body(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }
    }

    // 나머지 메서드들도 동일하게 CORS 헤더 추가...
    @GetMapping("/{studyGroupId}/poll")
    public ResponseEntity<List<Map<String, Object>>> pollNewMessages(
            @PathVariable Long studyGroupId,
            @RequestParam String since) {

        try {
            LocalDateTime sinceTime = LocalDateTime.parse(since);
            List<ChatMessage> newMessages = chatMessageRepository
                    .findByStudyGroupIdAndCreatedAtAfterAndIsDeletedFalse(
                            studyGroupId, sinceTime,
                            Sort.by("createdAt").ascending());

            List<Map<String, Object>> response = newMessages.stream().map(message -> {
                Map<String, Object> messageMap = new HashMap<>();
                messageMap.put("id", message.getId());
                messageMap.put("studyGroupId", message.getStudyGroup().getId());
                messageMap.put("content", message.getContent());
                messageMap.put("messageType", message.getMessageType());
                messageMap.put("fileName", message.getFileName());
                messageMap.put("fileUrl", message.getFileUrl());
                messageMap.put("createdAt", message.getCreatedAt().toString());
                messageMap.put("isDeleted", message.isDeleted());

                // Sender 정보
                Map<String, Object> senderMap = new HashMap<>();
                senderMap.put("id", message.getSender().getId());
                senderMap.put("name", message.getSender().getName());
                senderMap.put("email", message.getSender().getEmail());
                senderMap.put("createdAt", message.getSender().getCreatedAt().toString());
                messageMap.put("sender", senderMap);

                return messageMap;
            }).collect(Collectors.toList());

            return ResponseEntity.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .body(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }
    }

    @DeleteMapping("/message/{messageId}")
    public ResponseEntity<?> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam Long userId) {

        try {
            Optional<ChatMessage> messageOpt = chatMessageRepository.findById(messageId);

            if (!messageOpt.isPresent()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Message not found");
                return ResponseEntity.notFound().build();
            }

            ChatMessage message = messageOpt.get();

            // 메시지 작성자만 삭제 가능
            if (!message.getSender().getId().equals(userId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Only the sender can delete this message");
                return ResponseEntity.status(403)
                        .header("Access-Control-Allow-Origin", "*")
                        .body(errorResponse);
            }

            message.setDeleted(true);
            chatMessageRepository.save(message);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Message deleted successfully");
            return ResponseEntity.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .body(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete message: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .header("Access-Control-Allow-Origin", "*")
                    .body(errorResponse);
        }
    }
}