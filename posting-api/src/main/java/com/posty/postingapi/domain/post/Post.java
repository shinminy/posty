package com.posty.postingapi.domain.post;

import com.posty.postingapi.domain.series.Series;
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
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id", nullable = false)
    private Series series;

    @Column(nullable = false)
    private String title;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostBlock> blocks = new ArrayList<>();

    public void updateTitle(String title) {
        if (StringUtils.hasText(title)) {
            this.title = title;
        }
    }

    public void addBlock(PostBlock block) {
        this.blocks.add(block);
        block.setPost(this);
    }

    public void removeBlock(PostBlock block) {
        this.blocks.remove(block);
        block.setPost(null);
    }

    public void removeBlocks(List<Long> blockIds) {
        this.blocks.removeIf(block -> {
            if (blockIds.contains(block.getId())) {
                block.setPost(null);
                return true;
            } else {
                return false;
            }
        });
    }
}
