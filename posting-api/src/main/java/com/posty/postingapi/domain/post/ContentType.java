package com.posty.postingapi.domain.post;

// IMPORTANT: Enum 값 변경 시, ContentRequest와 ContentResponse의 @JsonSubTypes를 반드시 함께 업데이트해야 합니다.
// CAUTION: 변경 누락 시 직렬화/역직렬화 오류가 발생할 수 있습니다.
// LINK: 관련 클래스 - com.posty.postingapi.dto.post.ContentRequest, com.posty.postingapi.dto.post.ContentResponse
public enum ContentType {
    MEDIA,
    TEXT,
}
