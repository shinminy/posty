package com.posty.postingapi.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class PostSummary {

    private Long id;

    private String title;

    private LocalDateTime createdAt;
}
