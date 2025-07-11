package com.posty.postingapi.dto.series;

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
public class SeriesUpdateRequest {

    @Size(max = 32)
    private String title;

    @Size(max = 1000)
    private String description;

    @Size(min = 1)
    private List<Long> accountIds;

    public void normalize() {
        if (title != null) {
            title = title.trim();
        }

        if (description != null) {
            description = description.trim();
        }

        if (accountIds != null) {
            accountIds = new ArrayList<>(new HashSet<>(accountIds));
        }
    }
}
