package com.studyapp.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "study_groups")
public class StudyGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "study_group_members",
            joinColumns = @JoinColumn(name = "study_group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();  // 초기화 중요!

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "max_members")
    private Integer maxMembers = 50;  // 최대 멤버 수

    @Column(name = "is_active")
    private Boolean isActive = true;  // 활성 상태

    // 기본 생성자
    public StudyGroup() {
        this.createdAt = LocalDateTime.now();
        this.members = new HashSet<>();
    }

    // 생성자
    public StudyGroup(String name, String description, User creator) {
        this.name = name;
        this.description = description;
        this.creator = creator;
        this.createdAt = LocalDateTime.now();
        this.members = new HashSet<>();
        this.isActive = true;

        // 생성자를 멤버로 자동 추가
        if (creator != null) {
            this.members.add(creator);
        }
    }

    // 비즈니스 로직 메서드들
    public boolean addMember(User user) {
        if (user == null || !isActive) {
            return false;
        }
        if (maxMembers != null && members.size() >= maxMembers) {
            return false;  // 최대 인원 초과
        }
        return members.add(user);
    }

    public boolean removeMember(User user) {
        if (user == null) {
            return false;
        }
        // 생성자는 제거할 수 없음
        if (user.equals(creator)) {
            return false;
        }
        return members.remove(user);
    }

    public boolean isMember(User user) {
        return members != null && members.contains(user);
    }

    public int getMemberCount() {
        return members != null ? members.size() : 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Set<User> getMembers() {
        if (members == null) {
            members = new HashSet<>();
        }
        return members;
    }

    public void setMembers(Set<User> members) {
        this.members = members != null ? members : new HashSet<>();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}