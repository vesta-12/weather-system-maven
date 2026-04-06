package org.example.weathersystemmaven.dto;

public record WeatherResponse(
        double temperature,
        String condition
) {
}