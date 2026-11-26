package com.kratee.copilot.auth;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// LoginRequest.java
public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}