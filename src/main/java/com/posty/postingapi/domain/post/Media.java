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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaStatus status;

    @Column(nullable = false)
    private Integer tryCount;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastProcessedAt;

    public Media succeeded(String storedUrl, LocalDateTime lastProcessedAt) {
        return this.toBuilder()
                .status(MediaStatus.SUCCESS)
                .storedUrl(storedUrl)
                .tryCount(tryCount + 1)
                .lastProcessedAt(lastProcessedAt)
                .build();
    }

    public Media failed(LocalDateTime lastProcessedAt) {
        return this.toBuilder()
                .status(MediaStatus.FAILED)
                .tryCount(tryCount + 1)
                .lastProcessedAt(lastProcessedAt)
                .build();
    }

    public Media pending() {
        return this.toBuilder()
                .status(MediaStatus.PENDING)
                .build();
    }
}
