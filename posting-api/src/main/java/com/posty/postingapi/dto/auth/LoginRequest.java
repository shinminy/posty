package com.posty.postingapi.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class LoginRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 10, max = 128)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    private String password;

    public void normalize() {
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }
}
