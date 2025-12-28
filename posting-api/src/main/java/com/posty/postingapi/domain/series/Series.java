package com.posty.postingapi.domain.series;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.post.Post;
import com.posty.postingapi.dto.series.SeriesUpdateRequest;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Series {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "series_manager",
            joinColumns = @JoinColumn(name = "series_id"),
            inverseJoinColumns = @JoinColumn(name = "account_id")
    )
    private Set<Account> managers = new HashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL)
    private List<Post> posts = new ArrayList<>();

    public void addManager(Account account) {
        if (account == null || managers.contains(account)) {
            return;
        }

        managers.add(account);
        account.addManagedSeries(this);
    }

    public void removeManager(Account account) {
        if (account == null) {
            return;
        }

        if (managers.remove(account)) {
            account.removeManagedSeries(this);
        }
    }

    public void updateInfo(SeriesUpdateRequest request, Set<Account> managers) {
        if (StringUtils.hasText(request.getTitle())) {
            title = request.getTitle();
        }

        if (StringUtils.hasText(request.getDescription())) {
            description = request.getDescription();
        }

        if (managers != null) {
            this.managers = managers;
        }
    }
}
