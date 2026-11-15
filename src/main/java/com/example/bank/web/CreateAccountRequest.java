package com.example.bank.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(

        @NotBlank(message = "customerName is mandatory")
        @Size(max = 100, message = "customerName must be at most 100 characters")
        String customerName,

        // Optional: null is valid, but when supplied it must be 5-30 characters and not blank.
        @Size(min = 5, max = 30, message = "accountNickName must be between 5 and 30 characters")
        @Pattern(regexp = "(?s).*\\S.*", message = "accountNickName must not be blank")
        String accountNickName) {
}
