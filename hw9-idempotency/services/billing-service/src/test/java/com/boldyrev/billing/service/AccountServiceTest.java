package com.boldyrev.billing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.boldyrev.billing.entity.Account;
import com.boldyrev.billing.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

  @Mock private AccountRepository accountRepository;
  @InjectMocks private AccountService accountService;

  @Test
  void createIsSkippedForExistingAccount() {
    when(accountRepository.existsById(1L)).thenReturn(true);

    accountService.create(1L);

    verify(accountRepository, never()).save(any());
  }

  @Test
  void depositIncreasesBalance() {
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account(1L, "10.00")));

    assertThat(accountService.deposit(1L, new BigDecimal("5.50")).balance())
        .isEqualByComparingTo("15.50");
  }

  @Test
  void withdrawDecreasesBalance() {
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account(1L, "10.00")));

    assertThat(accountService.withdraw(1L, new BigDecimal("4")).balance())
        .isEqualByComparingTo("6");
  }

  @Test
  void withdrawBeyondBalanceIsRejectedAndBalanceKept() {
    Account account = account(1L, "10.00");
    when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
    BigDecimal tooMuch = new BigDecimal("10.01");

    assertThatThrownBy(() -> accountService.withdraw(1L, tooMuch))
        .isInstanceOf(InsufficientFundsException.class);
    assertThat(account.getBalance()).isEqualByComparingTo("10.00");
  }

  private static Account account(long userId, String balance) {
    Account account = new Account(userId);
    account.setBalance(new BigDecimal(balance));
    return account;
  }
}
