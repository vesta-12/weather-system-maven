package org.example.weathersystemmaven.service;

import org.example.weathersystemmaven.client.WeatherClient;
import org.example.weathersystemmaven.dto.WeatherResponse;
import org.example.weathersystemmaven.entity.AppUser;
import org.example.weathersystemmaven.entity.OrderEntity;
import org.example.weathersystemmaven.exception.UserNotFoundException;
import org.example.weathersystemmaven.exception.WeatherApiException;
import org.example.weathersystemmaven.repository.AppUserRepository;
import org.example.weathersystemmaven.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WeatherClient weatherClient;

    @Mock
    private ProductSuggestionService productSuggestionService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrderSuccessfully() {
        AppUser user = AppUser.builder()
                .id(1L)
                .name("Marina")
                .city("Almaty")
                .build();

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(weatherClient.getWeather("Almaty")).thenReturn(new WeatherResponse(9.0, "rain"));
        when(productSuggestionService.suggestProduct(any())).thenReturn("umbrella");
        when(orderRepository.save(any(OrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderEntity result = orderService.createOrder(1L);

        assertNotNull(result);
        assertEquals("umbrella", result.getProduct());
        assertEquals(user, result.getUser());
        assertNotNull(result.getCreatedAt());

        verify(appUserRepository).findById(1L);
        verify(weatherClient).getWeather("Almaty");
        verify(productSuggestionService).suggestProduct(any(WeatherResponse.class));
        verify(orderRepository).save(any(OrderEntity.class));
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(appUserRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> orderService.createOrder(99L));

        verify(appUserRepository).findById(99L);
        verifyNoInteractions(weatherClient, productSuggestionService, orderRepository);
    }

    @Test
    void shouldNotSaveOrderWhenWeatherApiFails() {
        AppUser user = AppUser.builder()
                .id(1L)
                .name("Marina")
                .city("Almaty")
                .build();

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(weatherClient.getWeather("Almaty"))
                .thenThrow(new WeatherApiException("Timeout while calling weather API"));

        assertThrows(WeatherApiException.class, () -> orderService.createOrder(1L));

        verify(appUserRepository).findById(1L);
        verify(weatherClient).getWeather("Almaty");
        verifyNoInteractions(productSuggestionService);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldPropagateDatabaseFailure() {
        AppUser user = AppUser.builder()
                .id(1L)
                .name("Marina")
                .city("Almaty")
                .build();

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(weatherClient.getWeather("Almaty")).thenReturn(new WeatherResponse(20.0, "sunny"));
        when(productSuggestionService.suggestProduct(any())).thenReturn("sunglasses");
        when(orderRepository.save(any(OrderEntity.class)))
                .thenThrow(new RuntimeException("Database save failed"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.createOrder(1L));
        assertEquals("Database save failed", ex.getMessage());

        verify(orderRepository).save(any(OrderEntity.class));
    }

    @Test
    void shouldCaptureSavedOrderValues() {
        AppUser user = AppUser.builder()
                .id(1L)
                .name("Marina")
                .city("Almaty")
                .build();

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(weatherClient.getWeather("Almaty")).thenReturn(new WeatherResponse(-3.0, "snow"));
        when(productSuggestionService.suggestProduct(any())).thenReturn("jacket");
        when(orderRepository.save(any(OrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.createOrder(1L);

        ArgumentCaptor<OrderEntity> captor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(captor.capture());

        OrderEntity savedOrder = captor.getValue();
        assertEquals(user, savedOrder.getUser());
        assertEquals("jacket", savedOrder.getProduct());
        assertNotNull(savedOrder.getCreatedAt());
    }
}