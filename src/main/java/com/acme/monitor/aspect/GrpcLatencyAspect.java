package com.acme.monitor.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * gRPC 链路延迟埋点 AOP
 */
@Aspect
@Component
public class GrpcLatencyAspect {
    @Autowired
    private MeterRegistry meterRegistry;

    @Around("execution(* com.acme..grpc..*(..))")
    public Object recordGrpcLatency(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return pjp.proceed();
        } finally {
            long latency = System.currentTimeMillis() - start;
            String method = pjp.getSignature().getName();
            meterRegistry.timer("unit_rtt_ms", "method", method).record(latency, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }
}
