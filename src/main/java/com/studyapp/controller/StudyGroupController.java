package com.studyapp.controller;

import com.studyapp.entity.StudyGroup;
import com.studyapp.entity.User;
import com.studyapp.repository.StudyGroupRepository;
import com.studyapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/study-groups")
@CrossOrigin(origins = "*")
public class StudyGroupController {

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<StudyGroup>> getAllStudyGroups() {
        return ResponseEntity.ok(studyGroupRepository.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<StudyGroup>> searchStudyGroups(@RequestParam String query) {
        return ResponseEntity.ok(studyGroupRepository.findByNameContainingIgnoreCase(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudyGroup(@PathVariable Long id) {
        Optional<StudyGroup> studyGroupOpt = studyGroupRepository.findById(id);
        if (studyGroupOpt.isPresent()) {
            return ResponseEntity.ok(studyGroupOpt.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> createStudyGroup(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String description = (String) request.get("description");
        Long creatorId = Long.valueOf(request.get("creatorId").toString());

        Optional<User> creatorOpt = userRepository.findById(creatorId);
        if (!creatorOpt.isPresent()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Creator not found");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        StudyGroup studyGroup = new StudyGroup(name, description, creatorOpt.get());
        studyGroupRepository.save(studyGroup);

        return ResponseEntity.ok(studyGroup);
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinStudyGroup(@PathVariable Long id, @RequestBody Map<String, Object> request) {
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

        if (studyGroup.getMembers() != null) {
            studyGroup.getMembers().add(user);
        }
        studyGroupRepository.save(studyGroup);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Joined successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<?> leaveStudyGroup(@PathVariable Long id, @RequestBody Map<String, Object> request) {
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

        if (studyGroup.getMembers() != null) {
            studyGroup.getMembers().remove(user);
        }
        studyGroupRepository.save(studyGroup);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Left successfully");
        return ResponseEntity.ok(response);
    }
}