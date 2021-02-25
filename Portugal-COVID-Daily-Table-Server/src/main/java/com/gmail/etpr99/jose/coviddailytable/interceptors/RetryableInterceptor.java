package com.gmail.etpr99.jose.coviddailytable.interceptors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;

@Aspect
@Component
public class RetryableInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryableInterceptor.class);

    @Around("execution(* *(..)) && @annotation(com.gmail.etpr99.jose.coviddailytable.annotations.Retryable)")
    public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {
        int count = 0;
        int numOfAttempts = 3;
        while (true) {
            try {
                return joinPoint.proceed();
            } catch (SocketTimeoutException ste) {
                count++;
                LOGGER.warn("Could not establish an HTTP connection in the " + joinPoint.getSignature().getName() +
                    " trying again " + ((numOfAttempts - count) + 1) + " more times!");
                if (count == numOfAttempts) throw ste;
            }
        }
    }
}
