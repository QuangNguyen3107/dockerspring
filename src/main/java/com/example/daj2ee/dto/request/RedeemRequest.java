package com.example.daj2ee.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RedeemRequest(
    @Min(value = 1, message = "Points to redeem must be at least 1")
    int points,

    @NotBlank(message = "Item to redeem is required")
    String item
) {}
