package com.example.springmail.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessReviewReminderRequest {

    @NotBlank
    @Email
    private String to;

    @NotBlank
    private String username;

    @NotBlank
    private String expireDate;

    @NotBlank
    private String persona;

    private String subject;
}
