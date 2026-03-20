package com.example.daj2ee.dto.response;

import java.time.LocalDate;

public record CheckInResponse(
    LocalDate checkInDate,
    int totalPoints,
    int pointsEarned
) {}
