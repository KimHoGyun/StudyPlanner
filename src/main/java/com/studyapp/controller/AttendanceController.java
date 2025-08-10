package com.studyapp.controller;

import com.studyapp.entity.Attendance;
import com.studyapp.entity.StudyGroup;
import com.studyapp.entity.User;
import com.studyapp.repository.AttendanceRepository;
import com.studyapp.repository.StudyGroupRepository;
import com.studyapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @PostMapping
    public ResponseEntity<?> markAttendance(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Long studyGroupId = Long.valueOf(request.get("studyGroupId").toString());
        String dateStr = (String) request.get("date");
        boolean present = (Boolean) request.get("present");

        LocalDate date = LocalDate.parse(dateStr);

        Optional<User> userOpt = userRepository.findById(userId);
        Optional<StudyGroup> studyGroupOpt = studyGroupRepository.findById(studyGroupId);

        if (!userOpt.isPresent() || !studyGroupOpt.isPresent()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "User or study group not found");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // 기존 출석 기록 확인
        Optional<Attendance> existingAttendance = attendanceRepository
                .findByUserIdAndStudyGroupIdAndAttendanceDate(userId, studyGroupId, date);

        if (existingAttendance.isPresent()) {
            existingAttendance.get().setPresent(present);
            attendanceRepository.save(existingAttendance.get());
        } else {
            Attendance attendance = new Attendance(userOpt.get(), studyGroupOpt.get(), date, present);
            attendanceRepository.save(attendance);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Attendance marked successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{studyGroupId}")
    public ResponseEntity<List<Attendance>> getAttendance(
            @PathVariable Long studyGroupId,
            @RequestParam String date) {
        LocalDate attendanceDate = LocalDate.parse(date);
        return ResponseEntity.ok(attendanceRepository.findByStudyGroupIdAndAttendanceDate(studyGroupId, attendanceDate));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Attendance>> getUserAttendance(@PathVariable Long userId) {
        return ResponseEntity.ok(attendanceRepository.findByUserId(userId));
    }
}