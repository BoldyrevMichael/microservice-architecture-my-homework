package com.boldyrev.notification.repository;

import com.boldyrev.notification.entity.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

  List<Message> findByUserIdOrderByIdDesc(long userId);
}
