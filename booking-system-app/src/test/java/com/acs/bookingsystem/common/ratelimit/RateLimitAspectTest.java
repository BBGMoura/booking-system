package com.acs.bookingsystem.common.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class RateLimitAspectTest {

  @Mock private RateLimiter rateLimiter;
  @Mock private ClientIpResolver clientIpResolver;
  @Mock private ProceedingJoinPoint joinPoint;
  @Mock private MethodSignature methodSignature;

  private RateLimitAspect aspect;

  static class Dummy {
    @RateLimit(bucket = "login", key = RateLimitKeyType.IP)
    void ipLimited() {}

    @RateLimit(
        bucket = "passwordReset",
        key = RateLimitKeyType.SPEL,
        keyExpression = "#request.email()")
    void spelLimited(DummyRequest request) {}
  }

  record DummyRequest(String email) {}

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    aspect = new RateLimitAspect(rateLimiter, clientIpResolver);
  }

  @AfterEach
  void tearDown() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  void resolvesIpKeyAndProceedsWhenWithinLimit() throws Throwable {
    Method method = Dummy.class.getDeclaredMethod("ipLimited");
    RateLimit rateLimit = method.getAnnotation(RateLimit.class);
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getMethod()).thenReturn(method);
    when(joinPoint.getArgs()).thenReturn(new Object[0]);
    when(joinPoint.proceed()).thenReturn("ok");

    MockHttpServletRequest servletRequest = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));
    when(clientIpResolver.resolve(any(HttpServletRequest.class))).thenReturn("1.2.3.4");

    Object result = aspect.enforceLimit(joinPoint, rateLimit);

    assertThat(result).isEqualTo("ok");
    verify(rateLimiter).checkLimit("login", "1.2.3.4");
    verify(joinPoint).proceed();
  }

  @Test
  void resolvesSpelKeyFromMethodArgument() throws Throwable {
    Method method = Dummy.class.getDeclaredMethod("spelLimited", DummyRequest.class);
    RateLimit rateLimit = method.getAnnotation(RateLimit.class);
    DummyRequest request = new DummyRequest("user@example.com");
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getMethod()).thenReturn(method);
    when(joinPoint.getArgs()).thenReturn(new Object[] {request});
    when(joinPoint.proceed()).thenReturn("ok");

    aspect.enforceLimit(joinPoint, rateLimit);

    verify(rateLimiter).checkLimit("passwordReset", "user@example.com");
  }

  @Test
  void propagatesRateLimitExceededExceptionWithoutProceeding() throws Throwable {
    Method method = Dummy.class.getDeclaredMethod("ipLimited");
    RateLimit rateLimit = method.getAnnotation(RateLimit.class);
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getMethod()).thenReturn(method);
    when(joinPoint.getArgs()).thenReturn(new Object[0]);

    MockHttpServletRequest servletRequest = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));
    when(clientIpResolver.resolve(any(HttpServletRequest.class))).thenReturn("1.2.3.4");
    doThrow(new RateLimitExceededException(5)).when(rateLimiter).checkLimit("login", "1.2.3.4");

    assertThrows(RateLimitExceededException.class, () -> aspect.enforceLimit(joinPoint, rateLimit));
    verify(joinPoint, never()).proceed();
  }
}
