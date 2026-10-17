package com.boldyrev.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// пользователь уже проверен шлюзом: ingress-nginx сходил в /auth/verify и подставил X-User-Id
public class GatewayUserFilter extends OncePerRequestFilter {

  public static final String USER_ID_HEADER = "X-User-Id";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String userId = request.getHeader(USER_ID_HEADER);
    if (userId != null && !userId.isBlank()) {
      var authentication =
          UsernamePasswordAuthenticationToken.authenticated(
              userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    chain.doFilter(request, response);
  }
}
