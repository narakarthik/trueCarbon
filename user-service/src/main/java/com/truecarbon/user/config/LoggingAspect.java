package com.truecarbon.user.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.truecarbon.user.service..*(..)) || " +
            "execution(* com.truecarbon.user.api..*(..))")
    // For trading service replace with:
    // "execution(* com.truecarbon.trading.service..*(..)) || " +
    // "execution(* com.truecarbon.trading.api..*(..))    || " +
    // "execution(* com.truecarbon.trading.client..*(..))"
    public Object log(ProceedingJoinPoint pjp) throws Throwable {
        String cls    = pjp.getSignature().getDeclaringType().getSimpleName();
        String method = pjp.getSignature().getName();
        log.info("[START] {}.{}()", cls, method);
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            log.info("[END]   {}.{}() — {}ms", cls, method, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            log.error("[ERROR] {}.{}() — {}ms — {}: {}",
                cls, method, System.currentTimeMillis() - start,
                ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }
}