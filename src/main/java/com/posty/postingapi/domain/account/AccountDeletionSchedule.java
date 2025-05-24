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
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
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

    public AccountDeletionSchedule withStatus(ScheduleStatus status) {
        return AccountDeletionSchedule.builder()
                .id(id)
                .account(account)
                .status(status)
                .scheduledAt(scheduledAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public AccountDeletionSchedule completedWith(Account account) {
        return AccountDeletionSchedule.builder()
                .id(id)
                .account(account == null ? this.account : account)
                .status(ScheduleStatus.COMPLETED)
                .scheduledAt(scheduledAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
