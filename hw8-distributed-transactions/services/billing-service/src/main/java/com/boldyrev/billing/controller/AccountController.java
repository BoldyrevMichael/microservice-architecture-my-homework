package com.boldyrev.billing.controller;

import com.boldyrev.billing.dto.AccountResponse;
import com.boldyrev.billing.dto.AmountRequest;
import com.boldyrev.billing.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// счёт только свой: идентификатор пользователя подставляет шлюз в X-User-Id
@RestController
@RequestMapping("/billing/account")
public class AccountController {

  private final AccountService accountService;

  @Autowired
  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  @GetMapping
  public AccountResponse get(@RequestHeader("X-User-Id") long userId) {
    return accountService.get(userId);
  }

  @PostMapping("/deposit")
  public AccountResponse deposit(
      @RequestHeader("X-User-Id") long userId, @Valid @RequestBody AmountRequest request) {
    return accountService.deposit(userId, request.amount());
  }

  @PostMapping("/withdraw")
  public AccountResponse withdraw(
      @RequestHeader("X-User-Id") long userId, @Valid @RequestBody AmountRequest request) {
    return accountService.withdraw(userId, request.amount());
  }
}
