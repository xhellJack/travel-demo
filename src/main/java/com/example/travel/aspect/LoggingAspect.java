package com.example.travel.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.example.travel.controller..*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // 打印请求方法与参数
        log.info("➡️ Entering: {}.{}() with arguments = {}", className, methodName, Arrays.toString(args));

        long start = System.currentTimeMillis();
        Object result;

        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            log.error("💥 Exception in {}.{}(): {}", className, methodName, ex.getMessage(), ex);
            throw ex;
        }

        long elapsedTime = System.currentTimeMillis() - start;

        // 如果返回值过长可以做个截断（可选）
        String resultStr = (result != null) ? result.toString() : "null";
        if (resultStr.length() > 500) {
            resultStr = resultStr.substring(0, 500) + "...(truncated)";
        }

        // 打印返回值和耗时
        log.info("⬅️ Exiting: {}.{}() | Result = {} | Time = {} ms",
                className, methodName, resultStr, elapsedTime);

        return result;
    }
}
