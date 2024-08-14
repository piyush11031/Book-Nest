package org.network.backend.auth;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RegistrationsRequest {
    @NotEmpty(message = "first name is mandatory")
    @NotBlank(message = "first name is mandatory")
    private String firstName;

    @NotEmpty(message = "last name is mandatory")
    @NotBlank(message = "last name is mandatory")
    private String lastName;

    @Email(message = "email not formatted")
    @NotEmpty(message = "email is mandatory")
    @NotBlank(message = "email is mandatory")
    private String email;

    @NotEmpty(message = "password is mandatory")
    @NotBlank(message = "password is mandatory")
    @Size(min = 8, message = "password should be 8 characters long")
    private String password;
}
