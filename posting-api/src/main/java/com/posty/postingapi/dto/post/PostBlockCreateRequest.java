package com.posty.postingapi.dto.post;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class PostBlockCreateRequest extends PostBlockRequest {

    public PostBlockCreateRequest(Integer orderNo, Long writerId, ContentRequest content) {
        super(orderNo, writerId, content);
    }
}
