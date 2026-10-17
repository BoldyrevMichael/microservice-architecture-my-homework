package com.boldyrev.billing.repository;

import com.boldyrev.billing.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {}
