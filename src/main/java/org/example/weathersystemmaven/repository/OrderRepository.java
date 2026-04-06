package org.example.weathersystemmaven.repository;
import org.example.weathersystemmaven.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
}