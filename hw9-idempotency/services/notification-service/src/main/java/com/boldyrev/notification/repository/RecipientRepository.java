package com.boldyrev.notification.repository;

import com.boldyrev.notification.entity.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {}
