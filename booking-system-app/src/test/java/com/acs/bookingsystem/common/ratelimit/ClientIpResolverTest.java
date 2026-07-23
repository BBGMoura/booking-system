package com.acs.bookingsystem.common.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ClientIpResolverTest {

  private final ClientIpResolver resolver = new ClientIpResolver();

  @Test
  void resolvesRemoteAddrFromRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("203.0.113.42");

    String result = resolver.resolve(request);

    assertThat(result).isEqualTo("203.0.113.42");
  }

  @Test
  void ignoresXForwardedForHeader() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("203.0.113.42");
    request.addHeader("X-Forwarded-For", "1.2.3.4");

    String result = resolver.resolve(request);

    assertThat(result).isEqualTo("203.0.113.42");
  }
}
