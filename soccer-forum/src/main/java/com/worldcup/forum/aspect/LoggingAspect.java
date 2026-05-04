package com.worldcup.forum.aspect;

import com.baomidou.mybatisplus.core.conditions.interfaces.Join;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /** 拦截标注了 {@link Loggable} 的方法；{@code @annotation(loggable)} 与参数名绑定 */
    @Before("@annotation(loggable)")
    public void logBefore(JoinPoint joinPoint, Loggable loggable) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        String tag = loggable.value().isEmpty() ? methodName : loggable.value();
        log.info("【{}】{} 调用，参数：{}", tag, methodName, args);
    }

    @AfterReturning(pointcut = "@annotation(loggable)", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Loggable loggable,Object result){
        String methodName = joinPoint.getSignature().getName();
        log.info("【{}】方法返回：{}", methodName, result);
    }

    @AfterThrowing(pointcut = "@annotation(loggable)", throwing = "error")
    public void logAfterThrowing(JoinPoint joinPoint, Loggable loggable, Throwable error) {
        String methodName = joinPoint.getSignature().getName();
        log.warn("【{}】方法异常：{}", methodName, error.getMessage());
    }

}
