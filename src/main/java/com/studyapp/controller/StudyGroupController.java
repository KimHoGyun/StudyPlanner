package com.studyapp.controller;

import com.studyapp.entity.StudyGroup;
import com.studyapp.entity.User;
import com.studyapp.repository.StudyGroupRepository;
import com.studyapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/study-groups")
@CrossOrigin(origins = "*")
public class StudyGroupController {

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllStudyGroups() {
        try {
            List<StudyGroup> groups = studyGroupRepository.findAll();

            // 각 그룹의 정보를 맵으로 변환 (순환 참조 방지)
            List<Map<String, Object>> response = groups.stream().map(group -> {
                Map<String, Object> groupMap = new HashMap<>();
                groupMap.put("id", group.getId());
                groupMap.put("name", group.getName());
                groupMap.put("description", group.getDescription());
                groupMap.put("memberCount", group.getMemberCount());
                groupMap.put("maxMembers", group.getMaxMembers());
                groupMap.put("isActive", group.getIsActive());
                groupMap.put("createdAt", group.getCreatedAt());

                // Creator 정보
                if (group.getCreator() != null) {
                    Map<String, Object> creatorMap = new HashMap<>();
                    creatorMap.put("id", group.getCreator().getId());
                    creatorMap.put("name", group.getCreator().getName());
                    creatorMap.put("email", group.getCreator().getEmail());
                    groupMap.put("creator", creatorMap);
                }

                return groupMap;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<StudyGroup>> searchStudyGroups(@RequestParam String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(studyGroupRepository.findByNameContainingIgnoreCase(query));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudyGroup(@PathVariable Long id) {
        try {
            Optional<StudyGroup> studyGroupOpt = studyGroupRepository.findById(id);
            if (studyGroupOpt.isPresent()) {
                StudyGroup group = studyGroupOpt.get();

                // 상세 정보 맵 생성
                Map<String, Object> response = new HashMap<>();
                response.put("id", group.getId());
                response.put("name", group.getName());
                response.put("description", group.getDescription());
                response.put("memberCount", group.getMemberCount());
                response.put("maxMembers", group.getMaxMembers());
                response.put("isActive", group.getIsActive());
                response.put("createdAt", group.getCreatedAt());

                // Creator 정보
                if (group.getCreator() != null) {
                    Map<String, Object> creatorMap = new HashMap<>();
                    creatorMap.put("id", group.getCreator().getId());
                    creatorMap.put("name", group.getCreator().getName());
                    creatorMap.put("email", group.getCreator().getEmail());
                    response.put("creator", creatorMap);
                }

                // Members 정보
                List<Map<String, Object>> membersList = group.getMembers().stream().map(member -> {
                    Map<String, Object> memberMap = new HashMap<>();
                    memberMap.put("id", member.getId());
                    memberMap.put("name", member.getName());
                    memberMap.put("email", member.getEmail());
                    return memberMap;
                }).collect(Collectors.toList());
                response.put("members", membersList);

                return ResponseEntity.ok(response);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createStudyGroup(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            Long creatorId = Long.valueOf(request.get("creatorId").toString());

            // 유효성 검증
            if (name == null || name.trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Study group name is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Optional<User> creatorOpt = userRepository.findById(creatorId);
            if (!creatorOpt.isPresent()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Creator not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            StudyGroup studyGroup = new StudyGroup(name, description, creatorOpt.get());

            // 추가 설정이 있다면 적용
            if (request.containsKey("maxMembers")) {
                studyGroup.setMaxMembers(Integer.valueOf(request.get("maxMembers").toString()));
            }

            studyGroupRepository.save(studyGroup);

            Map<String, Object> response = new HashMap<>();
            response.put("id", studyGroup.getId());
            response.put("name", studyGroup.getName());
            response.put("description", studyGroup.getDescription());
            response.put("message", "Study group created successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create study group: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{id}/join")
    @Transactional
    public ResponseEntity<?> joinStudyGroup(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());

            Optional<StudyGroup> studyGroupOpt = studyGroupRepository.findById(id);
            Optional<User> userOpt = userRepository.findById(userId);

            if (!studyGroupOpt.isPresent() || !userOpt.isPresent()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Study group or user not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            StudyGroup studyGroup = studyGroupOpt.get();
            User user = userOpt.get();

            // 이미 멤버인지 확인
            if (studyGroup.isMember(user)) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "User is already a member");
                return ResponseEntity.ok(response);
            }

            // 멤버 추가
            if (!studyGroup.addMember(user)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Cannot join group (max members reached or group inactive)");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            studyGroupRepository.save(studyGroup);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Joined successfully");
            response.put("memberCount", studyGroup.getMemberCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to join study group: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{id}/leave")
    @Transactional
    public ResponseEntity<?> leaveStudyGroup(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());

            Optional<StudyGroup> studyGroupOpt = studyGroupRepository.findById(id);
            Optional<User> userOpt = userRepository.findById(userId);

            if (!studyGroupOpt.isPresent() || !userOpt.isPresent()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Study group or user not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            StudyGroup studyGroup = studyGroupOpt.get();
            User user = userOpt.get();

            // 멤버 제거
            if (!studyGroup.removeMember(user)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Cannot leave group (not a member or you are the creator)");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            studyGroupRepository.save(studyGroup);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Left successfully");
            response.put("memberCount", studyGroup.getMemberCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to leave study group: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteStudyGroup(@PathVariable Long id, @RequestParam Long userId) {
        try {
            Optional<StudyGroup> studyGroupOpt = studyGroupRepository.findById(id);

            if (!studyGroupOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            StudyGroup studyGroup = studyGroupOpt.get();

            // 생성자만 삭제 가능
            if (!studyGroup.getCreator().getId().equals(userId)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Only the creator can delete this study group");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            studyGroupRepository.delete(studyGroup);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Study group deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete study group: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}