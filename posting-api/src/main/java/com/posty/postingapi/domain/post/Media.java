package com.posty.postingapi.domain.post;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    @Column(nullable = false)
    private String originUrl;

    private String storedUrl;

    private String storedFilename;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaStatus status;

    @Column(nullable = false)
    private Integer uploadAttemptCount;

    @Column(nullable = false)
    private Integer deleteAttemptCount;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastUploadAttemptAt;

    private LocalDateTime lastDeleteAttemptAt;

    public void uploaded(String storedUrl, String storedFilename, LocalDateTime lastProcessedAt) {
        status = MediaStatus.UPLOADED;
        this.storedUrl = storedUrl;
        this.storedFilename = storedFilename;
        uploadAttemptCount += 1;
        lastUploadAttemptAt = lastProcessedAt;
    }

    public void uploadFailed(LocalDateTime lastProcessedAt) {
        status = MediaStatus.UPLOAD_FAILED;
        uploadAttemptCount += 1;
        lastUploadAttemptAt = lastProcessedAt;
    }

    public void waitingUpload() {
        status = MediaStatus.WAITING_UPLOAD;
    }

    public void waitingDeletion() {
        status = MediaStatus.WAITING_DELETION;
    }

    public void deletionFailed(LocalDateTime lastProcessedAt) {
        status = MediaStatus.DELETION_FAILED;
        deleteAttemptCount += 1;
        lastDeleteAttemptAt = lastProcessedAt;
    }
}
