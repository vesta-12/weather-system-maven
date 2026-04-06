package org.example.weathersystemmaven.service;

import org.example.weathersystemmaven.client.WeatherClient;
import org.example.weathersystemmaven.dto.WeatherResponse;
import org.example.weathersystemmaven.entity.AppUser;
import org.example.weathersystemmaven.entity.OrderEntity;
import org.example.weathersystemmaven.repository.AppUserRepository;
import org.example.weathersystemmaven.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(OrderServiceCombinedScenarioTest.TestConfig.class)
class OrderServiceCombinedScenarioTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        WeatherClient weatherClient() {
            return Mockito.mock(WeatherClient.class);
        }
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WeatherClient weatherClient;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void shouldCreateOrderUsingRealDbAndMockedWeatherApi() {
        AppUser user = appUserRepository.save(
                AppUser.builder()
                        .name("Marina")
                        .city("Almaty")
                        .build()
        );

        when(weatherClient.getWeather("Almaty"))
                .thenReturn(new WeatherResponse(7.0, "rain"));

        OrderEntity createdOrder = orderService.createOrder(user.getId());

        assertNotNull(createdOrder.getId());
        assertEquals("umbrella", createdOrder.getProduct());
        assertEquals(user.getId(), createdOrder.getUser().getId());

        List<OrderEntity> allOrders = orderRepository.findAll();
        assertEquals(1, allOrders.size());
        assertEquals("umbrella", allOrders.get(0).getProduct());
        assertEquals(user.getId(), allOrders.get(0).getUser().getId());
    }
}