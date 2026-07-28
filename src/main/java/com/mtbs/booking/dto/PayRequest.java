package com.mtbs.booking.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Payment instruction. {@code token} is the mock card token — any value charges successfully
 * except the literal {@code "fail"}, which forces the failure path.
 */
public record PayRequest(@NotBlank String token) {
}
