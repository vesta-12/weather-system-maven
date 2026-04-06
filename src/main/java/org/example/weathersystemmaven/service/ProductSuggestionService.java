package org.example.weathersystemmaven.service;

import org.example.weathersystemmaven.dto.WeatherResponse;
import org.springframework.stereotype.Service;

@Service
public class ProductSuggestionService {

    public String suggestProduct(WeatherResponse weather) {
        return switch (weather.condition().toLowerCase()) {
            case "rain" -> "umbrella";
            case "sunny" -> "sunglasses";
            case "snow" -> "jacket";
            default -> throw new IllegalArgumentException("unsupported weather condition: " + weather.condition());
        };
    }
}