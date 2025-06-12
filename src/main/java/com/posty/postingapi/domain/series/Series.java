package com.posty.postingapi.domain.series;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.post.Post;
import com.posty.postingapi.dto.SeriesUpdateRequest;
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
@ToString
public class Series {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public Series updatedBy(SeriesUpdateRequest request, Set<Account> managers) {
        return Series.builder()
                .id(id)
                .title(StringUtils.hasText(request.getTitle()) ? request.getTitle() : title)
                .description(StringUtils.hasText(request.getDescription()) ? request.getDescription() : description)
                .managers(managers)
                .posts(new ArrayList<>(posts))
                .build();
    }
}
