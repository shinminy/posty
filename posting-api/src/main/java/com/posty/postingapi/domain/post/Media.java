package com.posty.postingapi.domain.post;

import com.posty.common.domain.post.MediaType;
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
@ToString
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

    public Media uploaded(String storedUrl, String storedFilename, LocalDateTime lastProcessedAt) {
        return this.toBuilder()
                .status(MediaStatus.UPLOADED)
                .storedUrl(storedUrl)
                .storedFilename(storedFilename)
                .uploadAttemptCount(uploadAttemptCount + 1)
                .lastUploadAttemptAt(lastProcessedAt)
                .build();
    }

    public Media uploadFailed(LocalDateTime lastProcessedAt) {
        return this.toBuilder()
                .status(MediaStatus.UPLOAD_FAILED)
                .uploadAttemptCount(uploadAttemptCount + 1)
                .lastUploadAttemptAt(lastProcessedAt)
                .build();
    }

    public Media waitingUpload() {
        return this.toBuilder()
                .status(MediaStatus.WAITING_UPLOAD)
                .build();
    }

    public Media waitingDeletion() {
        return this.toBuilder()
                .status(MediaStatus.WAITING_DELETION)
                .build();
    }

    public Media deletionFailed(LocalDateTime lastProcessedAt) {
        return this.toBuilder()
                .status(MediaStatus.DELETION_FAILED)
                .deleteAttemptCount(deleteAttemptCount + 1)
                .lastDeleteAttemptAt(lastProcessedAt)
                .build();
    }
}
