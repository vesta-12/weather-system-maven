package org.example.weathersystemmaven.service;

import org.example.weathersystemmaven.dto.WeatherResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductSuggestionServiceTest {

    private final ProductSuggestionService productSuggestionService = new ProductSuggestionService();

    @Test
    void shouldReturnUmbrellaForRain() {
        String product = productSuggestionService.suggestProduct(new WeatherResponse(12.0, "rain"));
        assertEquals("umbrella", product);
    }

    @Test
    void shouldReturnSunglassesForSunny() {
        String product = productSuggestionService.suggestProduct(new WeatherResponse(28.0, "sunny"));
        assertEquals("sunglasses", product);
    }

    @Test
    void shouldReturnJacketForSnow() {
        String product = productSuggestionService.suggestProduct(new WeatherResponse(-5.0, "snow"));
        assertEquals("jacket", product);
    }

    @Test
    void shouldIgnoreCase() {
        String product = productSuggestionService.suggestProduct(new WeatherResponse(10.0, "RAIN"));
        assertEquals("umbrella", product);
    }

    @Test
    void shouldThrowExceptionForUnsupportedCondition() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> productSuggestionService.suggestProduct(new WeatherResponse(15.0, "fog"))
        );

        assertTrue(ex.getMessage().contains("unsupported weather condition"));
    }
}