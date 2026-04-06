package org.example.weathersystemmaven.client;
import org.example.weathersystemmaven.dto.WeatherResponse;

public interface WeatherClient {
    WeatherResponse getWeather(String city);
}