package com.posty.postingapi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class EmailVerificationVerifyRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    String verificationCode;

    public void normalize() {
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }
}
