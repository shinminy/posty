package com.posty.postingapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class SeriesCreateRequest {

    @NotBlank
    @Size(min = 1, max = 32)
    private String title;

    @Size(max = 1000)
    private String description;

    @NotEmpty
    private List<Long> accountIds;

    public void normalize() {
        if (title != null) {
            title = title.trim().toLowerCase();
        }

        if (description != null) {
            description = description.trim().toLowerCase();
        }

        if (accountIds != null) {
            accountIds = new ArrayList<>(new HashSet<>(accountIds));
        }
    }
}
