package com.acs.bookingsystem.common.ratelimit;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.caffeine.CaffeineProxyManager;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

  private static final int MAX_CACHE_ENTRIES = 100_000;
  private static final Duration CACHE_EXPIRY = Duration.ofHours(2);

  @Bean
  public ProxyManager<String> rateLimitProxyManager() {
    Caffeine<Object, Object> caffeine = Caffeine.newBuilder().maximumSize(MAX_CACHE_ENTRIES);
    return new CaffeineProxyManager<>(caffeine, CACHE_EXPIRY);
  }
}
