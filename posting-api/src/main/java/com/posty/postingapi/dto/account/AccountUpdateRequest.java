package com.posty.postingapi.dto.account;

import jakarta.validation.constraints.Pattern;
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
    @ToString.Exclude
    private String password;

    @Size(min = 1, max = 32)
    private String name;

    @Pattern(regexp = "^\\+\\d{1,3}-\\d{1,4}-\\d{3,4}-\\d{4}$", message = "Mobile number must be in international format, e.g., +82-10-1234-5678.")
    private String mobileNumber;

    public void normalize() {
        if (name != null) {
            name = name.trim().toLowerCase();
        }
    }
}
