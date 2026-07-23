package com.acs.bookingsystem.common.ratelimit;

import java.time.Duration;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

  private Map<String, Bucket> buckets;

  @Getter
  @Setter
  public static class Bucket {
    private long capacity;
    private Duration refillPeriod;
  }
}
