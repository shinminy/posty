package com.posty.postingapi.domain.account;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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

    public void updateProfile(AccountUpdateRequest request, String hashedPassword) {
        if (StringUtils.hasText(hashedPassword)) {
            password = hashedPassword;
        }

        if (StringUtils.hasText(request.getName())) {
            name = request.getName();
        }

        if (StringUtils.hasText(request.getMobileNumber())) {
            mobileNumber = request.getMobileNumber();
        }
    }

    public void addManagedSeries(Series series) {
        if (series == null || managedSeries.contains(series)) {
            return;
        }

        managedSeries.add(series);
        series.addManager(this);
    }

    public void removeManagedSeries(Series series) {
        if (series == null) {
            return;
        }

        if (managedSeries.remove(series)) {
            series.removeManager(this);
        }
    }

    public void markWaitingForDeletion() {
        status = AccountStatus.WAITING_FOR_DELETION;
    }

    public void markDeleted(LocalDateTime deletedAt) {
        status = AccountStatus.DELETED;
        this.deletedAt = deletedAt;
    }

    public void markLocked(LocalDateTime lockedAt) {
        status = AccountStatus.LOCKED;
        this.lockedAt = lockedAt;
    }

    public void updateLastLogin(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
