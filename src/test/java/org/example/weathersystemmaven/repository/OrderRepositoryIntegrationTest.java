package org.example.weathersystemmaven.repository;

import org.example.weathersystemmaven.entity.AppUser;
import org.example.weathersystemmaven.entity.OrderEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryIntegrationTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldPersistUserAndOrderRelationship() {
        AppUser user = appUserRepository.save(
                AppUser.builder()
                        .name("Marina")
                        .city("Almaty")
                        .build()
        );

        OrderEntity order = orderRepository.save(
                OrderEntity.builder()
                        .user(user)
                        .product("umbrella")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        assertNotNull(user.getId());
        assertNotNull(order.getId());
        assertEquals(user.getId(), order.getUser().getId());
        assertEquals("umbrella", order.getProduct());

        OrderEntity found = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(user.getId(), found.getUser().getId());
    }
}