package org.apereo.cas.sentry;

import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.util.function.FunctionUtils;
import io.sentry.Sentry;
import io.sentry.SpanStatus;
import io.sentry.TransactionOptions;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.AnnotationUtils;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This is {@link SentryMonitoringAspect}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@Aspect
public class SentryMonitoringAspect implements Serializable {
    @Serial
    private static final long serialVersionUID = 7714367826154462931L;

    /**
     * Around pointcut to wrap all monitorable components in the CAS namespace.
     * Starts a span, and returns the result of the joinpoint in the end.
     *
     * @param joinPoint the join point
     * @return the object
     */
    @Around("allSentryComponents()")
    public Object aroundSentryMonitoredComponents(final ProceedingJoinPoint joinPoint) {
        val spanType = getSpanTypeFromJoinPoint(joinPoint);
        val transactionOptions = new TransactionOptions();
        transactionOptions.setBindToScope(true);
        val transaction = Sentry.startTransaction(spanType, joinPoint.getSignature().getName(), transactionOptions);
        var span = Sentry.getSpan();
        if (span == null) {
            span = Sentry.startTransaction(spanType, joinPoint.getSignature().getName());
        }
        val innerSpan = span.startChild(spanType, joinPoint.getSignature().getName());
        try {
            val taskName = joinPoint.getSignature().getDeclaringTypeName() + '.' + joinPoint.getSignature().getName();
            LOGGER.debug("[{}]: Started exit span [{}] to execute [{}]", spanType, span.getOperation(), taskName);
            return executeJoinpoint(joinPoint);
        } catch (final Exception e) {
            innerSpan.setThrowable(e);
            innerSpan.setStatus(SpanStatus.INTERNAL_ERROR);

            span.setThrowable(e);
            span.setStatus(SpanStatus.INTERNAL_ERROR);

            transaction.setThrowable(e);
            transaction.setStatus(SpanStatus.INTERNAL_ERROR);

            Sentry.captureException(e);

            throw e;
        } finally {
            LOGGER.debug("Span [{}] ended", span.getOperation());
            innerSpan.finish();
            span.finish();
            transaction.finish();
        }
    }

    private static String getSpanTypeFromJoinPoint(final ProceedingJoinPoint joinPoint) {
        val candidates = Stream.of(
            AnnotationUtils.findAnnotation(joinPoint.getSignature().getDeclaringType(), Monitorable.class),
            AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), Monitorable.class));
        return candidates.filter(Objects::nonNull).findFirst().map(Monitorable::type).orElse("CAS");
    }

    private static Object executeJoinpoint(final ProceedingJoinPoint joinPoint) {
        return FunctionUtils.doUnchecked(() -> {
            val args = joinPoint.getArgs();
            LOGGER.trace("Executing [{}]", joinPoint.getStaticPart().toLongString());
            return joinPoint.proceed(args);
        });
    }

    @Pointcut("within(org.apereo.cas..*) "
        + "&& !within(org.apereo.cas..*config..*) "
        + "&& !within(org.apereo.cas..*Configuration) "
        + "&& !within(org.apereo.cas.authentication.credential..*)"
        + "&& !@within(org.apereo.cas.monitor.NotMonitorable)"
        + "&& !@target(org.apereo.cas.monitor.NotMonitorable)"
    )
    private void allSentryComponents() {
    }
}
