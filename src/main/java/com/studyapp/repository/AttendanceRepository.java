package com.studyapp.repository;

import com.studyapp.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudyGroupIdAndAttendanceDate(Long studyGroupId, LocalDate date);
    Optional<Attendance> findByUserIdAndStudyGroupIdAndAttendanceDate(Long userId, Long studyGroupId, LocalDate date);
    List<Attendance> findByUserId(Long userId);
    List<Attendance> findByStudyGroupId(Long studyGroupId);
}