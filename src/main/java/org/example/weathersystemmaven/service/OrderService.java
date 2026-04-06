package org.example.weathersystemmaven.service;

import lombok.RequiredArgsConstructor;
import org.example.weathersystemmaven.client.WeatherClient;
import org.example.weathersystemmaven.dto.WeatherResponse;
import org.example.weathersystemmaven.entity.AppUser;
import org.example.weathersystemmaven.entity.OrderEntity;
import org.example.weathersystemmaven.exception.UserNotFoundException;
import org.example.weathersystemmaven.repository.AppUserRepository;
import org.example.weathersystemmaven.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final AppUserRepository appUserRepository;
    private final OrderRepository orderRepository;
    private final WeatherClient weatherClient;
    private final ProductSuggestionService productSuggestionService;

    @Transactional
    public OrderEntity createOrder(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        WeatherResponse weather = weatherClient.getWeather(user.getCity());
        String product = productSuggestionService.suggestProduct(weather);

        OrderEntity order = OrderEntity.builder()
                .user(user)
                .product(product)
                .createdAt(LocalDateTime.now())
                .build();

        return orderRepository.save(order);
    }
}