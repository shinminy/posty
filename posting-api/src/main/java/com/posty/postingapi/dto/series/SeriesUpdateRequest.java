package com.posty.postingapi.dto.series;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "계정(Account) ID")
    @Size(min = 1)
    private List<Long> managerIds;

    public void normalize() {
        if (title != null) {
            title = title.trim();
        }

        if (description != null) {
            description = description.trim();
        }

        if (managerIds != null) {
            managerIds = new ArrayList<>(new HashSet<>(managerIds));
        }
    }
}
