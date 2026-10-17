package com.boldyrev.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.UrlHandlerFilter;

@Configuration
public class WebConfig {

  // запрос с хвостовым слешем обрабатывается как без него
  @Bean
  public UrlHandlerFilter trailingSlashFilter() {
    return UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build();
  }
}
