package org.example.weathersystemmaven.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.weathersystemmaven.dto.WeatherResponse;
import org.example.weathersystemmaven.exception.WeatherApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class OpenMeteoWeatherClient implements WeatherClient {

    private final RestClient restClient;
    private final String geocodingBaseUrl;
    private final String forecastBaseUrl;

    @Autowired
    public OpenMeteoWeatherClient(
            @Value("${weather.api.geocoding-base-url}") String geocodingBaseUrl,
            @Value("${weather.api.forecast-base-url}") String forecastBaseUrl
    ) {
        this(RestClient.builder().build(), geocodingBaseUrl, forecastBaseUrl);
    }

    OpenMeteoWeatherClient(RestClient restClient, String geocodingBaseUrl, String forecastBaseUrl) {
        this.restClient = restClient;
        this.geocodingBaseUrl = geocodingBaseUrl;
        this.forecastBaseUrl = forecastBaseUrl;
    }

    @Override
    public WeatherResponse getWeather(String city) {
        try {
            GeocodingResponse geocodingResponse = restClient.get()
                    .uri(geocodingBaseUrl + "/v1/search?name={city}&count=1", city)
                    .retrieve()
                    .body(GeocodingResponse.class);

            if (geocodingResponse == null || geocodingResponse.results() == null || geocodingResponse.results().isEmpty()) {
                throw new WeatherApiException("City not found: " + city);
            }

            LocationResult location = geocodingResponse.results().get(0);

            ForecastResponse forecastResponse = restClient.get()
                    .uri(
                            forecastBaseUrl + "/v1/forecast?latitude={lat}&longitude={lon}&current=temperature_2m,weather_code",
                            location.latitude(),
                            location.longitude()
                    )
                    .retrieve()
                    .body(ForecastResponse.class);

            if (forecastResponse == null || forecastResponse.current() == null) {
                throw new WeatherApiException("Weather data is missing for city: " + city);
            }

            String condition = mapWeatherCodeToCondition(forecastResponse.current().weatherCode());

            return new WeatherResponse(
                    forecastResponse.current().temperature2m(),
                    condition
            );
        } catch (RestClientException e) {
            throw new WeatherApiException("Failed to fetch weather for city: " + city, e);
        }
    }

    private String mapWeatherCodeToCondition(int weatherCode) {
        if (isSnow(weatherCode)) return "snow";
        if (isRain(weatherCode)) return "rain";
        return "sunny";
    }

    private boolean isRain(int code) {
        return code == 51 || code == 53 || code == 55
                || code == 56 || code == 57
                || code == 61 || code == 63 || code == 65
                || code == 66 || code == 67
                || code == 80 || code == 81 || code == 82
                || code == 95 || code == 96 || code == 99;
    }

    private boolean isSnow(int code) {
        return code == 71 || code == 73 || code == 75
                || code == 77
                || code == 85 || code == 86;
    }

    private record GeocodingResponse(List<LocationResult> results) {}
    private record LocationResult(String name, double latitude, double longitude) {}
    private record ForecastResponse(CurrentWeather current) {}
    private record CurrentWeather(
            @JsonProperty("temperature_2m") double temperature2m,
            @JsonProperty("weather_code") int weatherCode
    ) {}
}