package com.boldyrev.order.idempotency;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

// отпечаток тела запроса: по нему видно, что ключ повторили с другими данными
public final class RequestHash {

  private RequestHash() {}

  public static String of(Object request) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256")
              .digest(request.toString().getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
