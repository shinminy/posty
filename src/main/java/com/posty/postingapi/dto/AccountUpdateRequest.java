package com.posty.postingapi.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class AccountUpdateRequest {

    @Size(min = 10, max = 128)
    private String password;

    private String name;

    private String mobileNumber;

    public void normalize() {
        if (this.name != null) {
            this.name = this.name.trim().toLowerCase();
        }
    }
}
