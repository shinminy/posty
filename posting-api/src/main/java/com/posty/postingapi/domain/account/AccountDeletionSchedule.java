package com.posty.postingapi.domain.account;

import com.posty.postingapi.domain.common.ScheduleStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AccountDeletionSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleStatus status;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static AccountDeletionSchedule create(Account account, LocalDateTime scheduledAt) {
        return AccountDeletionSchedule.builder()
                .account(account)
                .status(ScheduleStatus.SCHEDULED)
                .scheduledAt(scheduledAt)
                .build();
    }

    @Builder
    private AccountDeletionSchedule(Long id, Account account, ScheduleStatus status, LocalDateTime scheduledAt) {
        this.id = id;
        this.account = account;
        this.status = status;
        this.scheduledAt = scheduledAt;
    }

    public void markInProgress() {
        status = ScheduleStatus.IN_PROGRESS;
    }

    public void markCompleted(Account account) {
        if (account != null) {
            this.account = account;
        }

        status = ScheduleStatus.COMPLETED;
    }

    public void markFailed() {
        status = ScheduleStatus.FAILED;
    }
}
