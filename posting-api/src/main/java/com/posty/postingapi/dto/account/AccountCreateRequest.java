package com.posty.postingapi.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class AccountCreateRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 10, max = 128)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    private String password;

    @NotBlank
    @Size(min = 1, max = 32)
    private String name;

    @Pattern(regexp = "^\\+\\d{1,3}-\\d{1,4}-\\d{3,4}-\\d{4}$", message = "Mobile number must be in international format, e.g., +82-10-1234-5678.")
    private String mobileNumber;

    public void normalize() {
        if (email != null) {
            email = email.trim().toLowerCase();
        }

        if (name != null) {
            name = name.trim().toLowerCase();
        }
    }
}
