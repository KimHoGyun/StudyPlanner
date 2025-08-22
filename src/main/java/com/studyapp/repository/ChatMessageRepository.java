package com.studyapp.repository;

import com.studyapp.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 스터디 그룹의 메시지 조회 (삭제되지 않은 것만)
    List<ChatMessage> findByStudyGroupIdAndIsDeletedFalse(Long studyGroupId, Pageable pageable);

    // 특정 메시지 ID 이전의 메시지들 조회 (페이징)
    List<ChatMessage> findByStudyGroupIdAndIdLessThanAndIsDeletedFalse(
            Long studyGroupId, Long lastMessageId, Pageable pageable);

    // 특정 시간 이후의 새 메시지들 조회 (폴링용)
    List<ChatMessage> findByStudyGroupIdAndCreatedAtAfterAndIsDeletedFalse(
            Long studyGroupId, LocalDateTime since, Sort sort);

    // 스터디 그룹의 전체 메시지 수
    long countByStudyGroupIdAndIsDeletedFalse(Long studyGroupId);

    // 사용자가 보낸 메시지들
    List<ChatMessage> findBySenderIdAndIsDeletedFalse(Long senderId, Pageable pageable);

    // 특정 타입의 메시지들 조회
    List<ChatMessage> findByStudyGroupIdAndMessageTypeAndIsDeletedFalse(
            Long studyGroupId, String messageType, Pageable pageable);

    // 파일이 첨부된 메시지들 조회
    @Query("SELECT m FROM ChatMessage m WHERE m.studyGroup.id = :studyGroupId " +
            "AND m.fileUrl IS NOT NULL AND m.isDeleted = false ORDER BY m.createdAt DESC")
    List<ChatMessage> findMessagesWithFiles(@Param("studyGroupId") Long studyGroupId, Pageable pageable);

    // 최근 메시지 조회
    @Query("SELECT m FROM ChatMessage m WHERE m.studyGroup.id = :studyGroupId " +
            "AND m.isDeleted = false ORDER BY m.createdAt DESC")
    List<ChatMessage> findRecentMessages(@Param("studyGroupId") Long studyGroupId, Pageable pageable);
}

