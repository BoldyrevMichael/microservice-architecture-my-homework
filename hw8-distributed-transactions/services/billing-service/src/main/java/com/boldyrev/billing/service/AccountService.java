package com.boldyrev.billing.service;

import com.boldyrev.billing.dto.AccountResponse;
import com.boldyrev.billing.entity.Account;
import com.boldyrev.billing.repository.AccountRepository;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

  private final AccountRepository accountRepository;

  @Autowired
  public AccountService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  // счёт заводится по событию регистрации; повторное событие ничего не ломает
  @Transactional
  public void create(long userId) {
    if (!accountRepository.existsById(userId)) {
      accountRepository.save(new Account(userId));
    }
  }

  @Transactional(readOnly = true)
  public AccountResponse get(long userId) {
    return AccountResponse.from(find(userId));
  }

  @Transactional
  public AccountResponse deposit(long userId, BigDecimal amount) {
    Account account = find(userId);
    account.setBalance(account.getBalance().add(amount));
    return AccountResponse.from(account);
  }

  @Transactional
  public AccountResponse withdraw(long userId, BigDecimal amount) {
    Account account = find(userId);
    if (account.getBalance().compareTo(amount) < 0) {
      throw new InsufficientFundsException();
    }
    account.setBalance(account.getBalance().subtract(amount));
    return AccountResponse.from(account);
  }

  private Account find(long userId) {
    return accountRepository
        .findById(userId)
        .orElseThrow(() -> new AccountNotFoundException(userId));
  }
}
