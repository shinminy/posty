package com.posty.postingapi.dto.post;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class PostBlockUpdateRequest extends PostBlockRequest {

    @NotNull
    private Long id;

    public PostBlockUpdateRequest(Long id, Integer orderNo, Long writerId, ContentRequest content) {
        super(orderNo, writerId, content);

        this.id = id;
    }
}
