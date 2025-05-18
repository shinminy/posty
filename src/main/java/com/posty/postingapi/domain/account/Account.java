package com.posty.postingapi.domain.account;

import com.posty.postingapi.domain.post.Series;
import com.posty.postingapi.dto.AccountUpdateRequest;
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
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
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

    public Account(String email, String password, String name, String mobileNumber, AccountStatus status) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.status = status;
    }

    public Account withUpdatedFields(AccountUpdateRequest request, String hashedPassword) {
        return Account.builder()
                .id(this.id)
                .email(this.email)
                .password(StringUtils.hasText(hashedPassword) ? hashedPassword : this.password)
                .name(StringUtils.hasText(request.getName()) ? request.getName() : this.name)
                .mobileNumber(StringUtils.hasText(request.getMobileNumber()) ? request.getMobileNumber() : this.mobileNumber)
                .managedSeries(new ArrayList<>(this.managedSeries))
                .status(this.status)
                .lastLoginAt(this.lastLoginAt)
                .lockedAt(this.lockedAt)
                .deletedAt(this.deletedAt)
                .build();
    }
}
