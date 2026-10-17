package com.boldyrev.billing.dto;

import com.boldyrev.billing.entity.Account;
import java.math.BigDecimal;

public record AccountResponse(long userId, BigDecimal balance) {

  public static AccountResponse from(Account account) {
    return new AccountResponse(account.getUserId(), account.getBalance());
  }
}
