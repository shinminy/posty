package com.posty.postingapi.dto;

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
public class AccountCreateRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 10, max = 128)
    private String password;

    @NotBlank
    private String name;

    private String mobileNumber;
}
