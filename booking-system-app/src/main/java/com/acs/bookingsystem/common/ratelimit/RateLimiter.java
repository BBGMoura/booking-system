package com.acs.bookingsystem.common.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RateLimiter {

  private final RateLimitProperties properties;
  private final ProxyManager<String> proxyManager;

  public void checkLimit(String bucketName, String key) {
    RateLimitProperties.Bucket config = properties.getBuckets().get(bucketName);
    BucketProxy bucket =
        proxyManager.builder().build(bucketName + ":" + key, () -> buildConfiguration(config));

    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
    if (!probe.isConsumed()) {
      long retryAfterSeconds =
          Math.max(1, (long) Math.ceil(probe.getNanosToWaitForRefill() / 1_000_000_000.0));
      throw new RateLimitExceededException(retryAfterSeconds);
    }
  }

  private BucketConfiguration buildConfiguration(RateLimitProperties.Bucket config) {
    Bandwidth limit =
        Bandwidth.builder()
            .capacity(config.getCapacity())
            .refillGreedy(config.getCapacity(), config.getRefillPeriod())
            .build();
    return BucketConfiguration.builder().addLimit(limit).build();
  }
}
