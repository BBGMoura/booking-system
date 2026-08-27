package com.acs.bookingsystem.common.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.TimeMeter;
import io.github.bucket4j.caffeine.CaffeineProxyManager;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RateLimiterTest {

  private FakeTimeMeter clock;
  private RateLimiter rateLimiter;

  @BeforeEach
  void setUp() {
    clock = new FakeTimeMeter();

    RateLimitProperties.Bucket bucketConfig = new RateLimitProperties.Bucket();
    bucketConfig.setCapacity(3);
    bucketConfig.setRefillPeriod(Duration.ofMinutes(1));

    RateLimitProperties properties = new RateLimitProperties();
    properties.setBuckets(Map.of("login", bucketConfig));

    ProxyManager<String> proxyManager =
        new CaffeineProxyManager<>(
            Caffeine.newBuilder().maximumSize(100),
            Duration.ofHours(1),
            ClientSideConfig.getDefault().withClientClock(clock));

    rateLimiter = new RateLimiter(properties, proxyManager);
  }

  @Test
  void allowsRequestsUpToCapacity() {
    assertThatCode(
            () -> {
              rateLimiter.checkLimit("login", "1.2.3.4");
              rateLimiter.checkLimit("login", "1.2.3.4");
              rateLimiter.checkLimit("login", "1.2.3.4");
            })
        .doesNotThrowAnyException();
  }

  @Test
  void blocksRequestBeyondCapacity() {
    rateLimiter.checkLimit("login", "1.2.3.4");
    rateLimiter.checkLimit("login", "1.2.3.4");
    rateLimiter.checkLimit("login", "1.2.3.4");

    assertThrows(
        RateLimitExceededException.class, () -> rateLimiter.checkLimit("login", "1.2.3.4"));
  }

  @Test
  void independentKeysGetIndependentBuckets() {
    rateLimiter.checkLimit("login", "1.1.1.1");
    rateLimiter.checkLimit("login", "1.1.1.1");
    rateLimiter.checkLimit("login", "1.1.1.1");

    assertThatCode(() -> rateLimiter.checkLimit("login", "2.2.2.2")).doesNotThrowAnyException();
  }

  @Test
  void throwsWhenBucketNameNotConfigured() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> rateLimiter.checkLimit("unknownBucket", "1.2.3.4"));

    assertThat(exception.getMessage()).contains("unknownBucket");
  }

  @Test
  void refillsTokensAfterTimePasses() {
    rateLimiter.checkLimit("login", "1.2.3.4");
    rateLimiter.checkLimit("login", "1.2.3.4");
    rateLimiter.checkLimit("login", "1.2.3.4");
    assertThrows(
        RateLimitExceededException.class, () -> rateLimiter.checkLimit("login", "1.2.3.4"));

    clock.advance(Duration.ofSeconds(20));

    assertThatCode(() -> rateLimiter.checkLimit("login", "1.2.3.4")).doesNotThrowAnyException();
  }

  private static class FakeTimeMeter implements TimeMeter {
    private long nanos = 0;

    void advance(Duration duration) {
      nanos += duration.toNanos();
    }

    @Override
    public long currentTimeNanos() {
      return nanos;
    }

    @Override
    public boolean isWallClockBased() {
      return false;
    }
  }
}
