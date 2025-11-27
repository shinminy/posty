package com.posty.postingapi.domain.account;

import com.posty.postingapi.domain.series.Series;
import com.posty.postingapi.dto.account.AccountUpdateRequest;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    @ToString.Exclude
    private String password;

    @Column(nullable = false)
    private String name;

    private String mobileNumber;

    @Builder.Default
    @ManyToMany(mappedBy = "managers")
    private List<Series> managedSeries = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    private LocalDateTime lockedAt;

    private LocalDateTime deletedAt;

    public Account updatedBy(AccountUpdateRequest request, String hashedPassword) {
        return Account.builder()
                .id(id)
                .email(email)
                .password(StringUtils.hasText(hashedPassword) ? hashedPassword : password)
                .name(StringUtils.hasText(request.getName()) ? request.getName() : name)
                .mobileNumber(StringUtils.hasText(request.getMobileNumber()) ? request.getMobileNumber() : mobileNumber)
                .managedSeries(new ArrayList<>(managedSeries))
                .status(status)
                .lastLoginAt(lastLoginAt)
                .lockedAt(lockedAt)
                .deletedAt(deletedAt)
                .build();
    }

    public Account waitingForDeleting() {
        return Account.builder()
                .id(id)
                .email(email)
                .password(password)
                .name(name)
                .mobileNumber(mobileNumber)
                .managedSeries(new ArrayList<>(managedSeries))
                .status(AccountStatus.WAITING_FOR_DELETION)
                .lastLoginAt(lastLoginAt)
                .lockedAt(lockedAt)
                .deletedAt(deletedAt)
                .build();
    }

    public Account deleted(LocalDateTime deletedAt) {
        return Account.builder()
                .id(id)
                .email(email)
                .password(password)
                .name(name)
                .mobileNumber(mobileNumber)
                .managedSeries(new ArrayList<>(managedSeries))
                .status(AccountStatus.DELETED)
                .lastLoginAt(lastLoginAt)
                .lockedAt(lockedAt)
                .deletedAt(deletedAt == null ? this.deletedAt : deletedAt)
                .build();
    }

    public Account locked(LocalDateTime lockedAt) {
        return Account.builder()
                .id(id)
                .email(email)
                .password(password)
                .name(name)
                .mobileNumber(mobileNumber)
                .managedSeries(new ArrayList<>(managedSeries))
                .status(AccountStatus.LOCKED)
                .lastLoginAt(lastLoginAt)
                .lockedAt(lockedAt == null ? this.lockedAt : lockedAt)
                .deletedAt(deletedAt)
                .build();
    }
}
