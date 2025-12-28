package com.posty.postingapi.domain.post;

import com.posty.postingapi.domain.account.Account;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class PostBlock {

    public static final Sort SORT = Sort.by(Sort.Direction.ASC, "orderNo");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // IMPORTANT: 필드명 변경 시, SORT를 반드시 함께 업데이트해야 합니다.
    // CAUTION: 변경 누락 시 PostBlockRepository.findPageByPostId에서 오류가 발생할 수 있습니다.
    // LINK: PostBlock.SORT
    @Column(nullable = false)
    private Integer orderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private Account writer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType contentType;

    private String textContent;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "media_id")
    private Media media;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Post 양방향 관계 유지용 (오용 방지를 위해 같은 패키지에서만 접근 가능하도록 접근 제어자 생략)
    void setPost(Post post) {
        this.post = post;
    }

    public void updateMeta(Integer orderNo, Account writer) {
        this.orderNo = orderNo;
        this.writer = writer;
    }

    public void updateContentAsText(String text) {
        this.contentType = ContentType.TEXT;
        this.textContent = text;
        this.media = null;
    }

    public void updateContentAsMedia(Media media) {
        this.contentType = ContentType.MEDIA;
        this.media = media;
        this.textContent = null;
    }

    public boolean hasSameContent(PostBlock other) {
        if (this.contentType != other.contentType) {
            return false;
        }

        if (this.contentType == ContentType.TEXT) {
            String t1 = this.textContent;
            String t2 = other.textContent;
            return (t1 == null && t2 == null) || (t1 != null && t1.equals(t2));
        }

        if (this.media == null && other.media == null) {
            return true;
        }
        if (this.media == null || other.media == null) {
            return false;
        }

        boolean sameType = this.media.getMediaType() == other.media.getMediaType();
        String u1 = this.media.getOriginUrl();
        String u2 = other.media.getOriginUrl();
        boolean sameUrl = (u1 == null && u2 == null) || (u1 != null && u1.equals(u2));

        return sameType && sameUrl;
    }
}
