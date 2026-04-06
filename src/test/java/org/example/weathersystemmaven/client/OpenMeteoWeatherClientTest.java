package org.example.weathersystemmaven.client;

import org.example.weathersystemmaven.dto.WeatherResponse;
import org.example.weathersystemmaven.exception.WeatherApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenMeteoWeatherClientTest {

    private OpenMeteoWeatherClient weatherClient;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder.build();
        weatherClient = new OpenMeteoWeatherClient(
                restClient,
                "https://geocoding.test",
                "https://forecast.test"
        );
    }

    @Test
    void shouldReturnRainWeather() {
        server.expect(requestTo(containsString("https://geocoding.test/v1/search?name=Almaty&count=1")))
                .andRespond(withSuccess("""
                        {
                          "results": [
                            {
                              "name": "Almaty",
                              "latitude": 43.25,
                              "longitude": 76.95
                            }
                          ]
                        }
                        """, APPLICATION_JSON));

        server.expect(requestTo(allOf(
                        containsString("https://forecast.test/v1/forecast"),
                        containsString("latitude=43.25"),
                        containsString("longitude=76.95"),
                        containsString("current=temperature_2m,weather_code")
                )))
                .andRespond(withSuccess("""
                        {
                          "current": {
                            "temperature_2m": 11.5,
                            "weather_code": 61
                          }
                        }
                        """, APPLICATION_JSON));

        WeatherResponse result = weatherClient.getWeather("Almaty");

        assertEquals(11.5, result.temperature());
        assertEquals("rain", result.condition());

        server.verify();
    }

    @Test
    void shouldThrowWhenCityNotFound() {
        server.expect(requestTo(containsString("https://geocoding.test/v1/search?name=UnknownCity&count=1")))
                .andRespond(withSuccess("""
                        {
                          "results": []
                        }
                        """, APPLICATION_JSON));

        assertThrows(WeatherApiException.class, () -> weatherClient.getWeather("UnknownCity"));
    }

    @Test
    void shouldThrowWhenForecastResponseIsInvalid() {
        server.expect(requestTo(containsString("https://geocoding.test/v1/search?name=Astana&count=1")))
                .andRespond(withSuccess("""
                        {
                          "results": [
                            {
                              "name": "Astana",
                              "latitude": 51.17,
                              "longitude": 71.43
                            }
                          ]
                        }
                        """, APPLICATION_JSON));

        server.expect(requestTo(allOf(
                        containsString("https://forecast.test/v1/forecast"),
                        containsString("latitude=51.17"),
                        containsString("longitude=71.43")
                )))
                .andRespond(withSuccess("""
                        {
                          "wrong_field": {}
                        }
                        """, APPLICATION_JSON));

        assertThrows(WeatherApiException.class, () -> weatherClient.getWeather("Astana"));
    }

    @Test
    void shouldThrowWhenApiReturnsServerError() {
        server.expect(requestTo(containsString("https://geocoding.test/v1/search?name=Almaty&count=1")))
                .andRespond(withServerError());

        assertThrows(WeatherApiException.class, () -> weatherClient.getWeather("Almaty"));
    }
}