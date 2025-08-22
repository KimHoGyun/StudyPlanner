package com.studyapp.repository;

import com.studyapp.entity.FileAttachment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    // 스터디 그룹의 파일들 조회 (최신순)
    List<FileAttachment> findByStudyGroupIdOrderByUploadedAtDesc(Long studyGroupId);

    // 사용자가 업로드한 파일들
    List<FileAttachment> findByUploaderIdOrderByUploadedAtDesc(Long uploaderId);

    // 특정 기간 내 업로드된 파일들
    List<FileAttachment> findByStudyGroupIdAndUploadedAtBetween(
            Long studyGroupId, LocalDateTime start, LocalDateTime end);

    // 파일 타입별 조회
    List<FileAttachment> findByStudyGroupIdAndFileTypeContaining(Long studyGroupId, String fileType);

    // 이미지 파일들만 조회
    @Query("SELECT f FROM FileAttachment f WHERE f.studyGroup.id = :studyGroupId " +
            "AND f.fileType LIKE 'image/%' ORDER BY f.uploadedAt DESC")
    List<FileAttachment> findImagesByStudyGroupId(@Param("studyGroupId") Long studyGroupId);

    // 문서 파일들만 조회
    @Query("SELECT f FROM FileAttachment f WHERE f.studyGroup.id = :studyGroupId " +
            "AND (f.fileType LIKE '%document%' OR f.fileType LIKE '%pdf%' OR f.fileType LIKE '%text%') " +
            "ORDER BY f.uploadedAt DESC")
    List<FileAttachment> findDocumentsByStudyGroupId(@Param("studyGroupId") Long studyGroupId);

    // 파일 크기별 조회
    List<FileAttachment> findByStudyGroupIdAndFileSizeLessThan(Long studyGroupId, Long maxSize);

    // 파일명으로 검색
    @Query("SELECT f FROM FileAttachment f WHERE f.studyGroup.id = :studyGroupId " +
            "AND LOWER(f.fileName) LIKE LOWER(CONCAT('%', :fileName, '%')) " +
            "ORDER BY f.uploadedAt DESC")
    List<FileAttachment> findByStudyGroupIdAndFileNameContaining(
            @Param("studyGroupId") Long studyGroupId, @Param("fileName") String fileName);

    // 스터디 그룹의 총 파일 개수
    long countByStudyGroupId(Long studyGroupId);

    // 스터디 그룹의 총 파일 크기
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileAttachment f WHERE f.studyGroup.id = :studyGroupId")
    Long getTotalFileSizeByStudyGroupId(@Param("studyGroupId") Long studyGroupId);
}