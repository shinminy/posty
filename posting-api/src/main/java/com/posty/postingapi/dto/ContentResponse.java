package com.posty.postingapi.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.posty.postingapi.domain.post.ContentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextContentResponse.class, name = "TEXT"),
        @JsonSubTypes.Type(value = MediaContentResponse.class, name = "MEDIA")
})
@AllArgsConstructor
@Getter
@Setter
@ToString
public abstract class ContentResponse {

    // IMPORTANT: 'type' 필드명 변경 시, @JsonTypeInfo(property = "type")도 반드시 동일하게 변경해야 합니다.
    // CAUTION: 미반영 시 Jackson 직렬화/역직렬화 오류가 발생할 수 있습니다.
    private ContentType type;
}
