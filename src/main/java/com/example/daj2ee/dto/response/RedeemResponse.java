package com.example.daj2ee.dto.response;

public record RedeemResponse(
    String item,
    int pointsRedeemed,
    int remainingPoints,
    String message
) {}
