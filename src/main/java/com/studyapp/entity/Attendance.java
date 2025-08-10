package com.studyapp.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendance")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "study_group_id")
    private StudyGroup studyGroup;

    @Column(name = "attendance_date")
    private LocalDate attendanceDate;

    private boolean present;

    // 기본 생성자
    public Attendance() {}

    // 생성자
    public Attendance(User user, StudyGroup studyGroup, LocalDate attendanceDate, boolean present) {
        this.user = user;
        this.studyGroup = studyGroup;
        this.attendanceDate = attendanceDate;
        this.present = present;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public StudyGroup getStudyGroup() { return studyGroup; }
    public void setStudyGroup(StudyGroup studyGroup) { this.studyGroup = studyGroup; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public boolean isPresent() { return present; }
    public void setPresent(boolean present) { this.present = present; }
}