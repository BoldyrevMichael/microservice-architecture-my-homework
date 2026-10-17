package com.boldyrev.order.repository;

import com.boldyrev.order.entity.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

  List<Order> findByUserIdOrderByIdDesc(long userId);

  Optional<Order> findByIdAndUserId(long id, long userId);
}
