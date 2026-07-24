package com.acs.bookingsystem.common.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

  private final RateLimiter rateLimiter;
  private final ClientIpResolver clientIpResolver;
  private final ParameterNameDiscoverer parameterNameDiscoverer =
      new DefaultParameterNameDiscoverer();
  private final ExpressionParser expressionParser = new SpelExpressionParser();

  @Around("@annotation(rateLimit)")
  public Object enforceLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
    String key = resolveKey(joinPoint, rateLimit);
    rateLimiter.checkLimit(rateLimit.bucket(), key);
    return joinPoint.proceed();
  }

  private String resolveKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
    return switch (rateLimit.key()) {
      case IP -> resolveIp();
      case SPEL -> resolveSpel(joinPoint, rateLimit.keyExpression());
    };
  }

  private String resolveIp() {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    HttpServletRequest request = attributes.getRequest();
    return clientIpResolver.resolve(request);
  }

  private String resolveSpel(ProceedingJoinPoint joinPoint, String expression) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String[] paramNames = parameterNameDiscoverer.getParameterNames(signature.getMethod());
    Object[] args = joinPoint.getArgs();

    EvaluationContext context = new StandardEvaluationContext();
    for (int i = 0; i < paramNames.length; i++) {
      context.setVariable(paramNames[i], args[i]);
    }
    return expressionParser.parseExpression(expression).getValue(context, String.class);
  }
}
