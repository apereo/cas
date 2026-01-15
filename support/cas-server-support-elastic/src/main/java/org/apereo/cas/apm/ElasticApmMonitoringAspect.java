package org.apereo.cas.apm;

import module java.base;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.util.function.FunctionUtils;
import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Outcome;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * This is {@link ElasticApmMonitoringAspect}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Aspect
@Slf4j
@SuppressWarnings("UnusedMethod")
public class ElasticApmMonitoringAspect implements Serializable {
    @Serial
    private static final long serialVersionUID = -6233930180591815087L;

    /**
     * Around pointcut to wrap all monitorable components in the CAS namespace.
     * Starts an exit span, and returns the result of the joinpoint in the end.
     *
     * @param joinPoint the join point
     * @return the object
     */
    @Around("allComponents()")
    public Object aroundElasticApmMonitoredComponents(final ProceedingJoinPoint joinPoint) {
        val parentTransaction = ElasticApm.currentTransaction();

        val spanType = getSpanTypeFromJoinPoint(joinPoint);
        val span = parentTransaction.startExitSpan(spanType,
            joinPoint.getTarget().getClass().getSimpleName(), joinPoint.getSignature().getName());
        try {
            val taskName = joinPoint.getSignature().getDeclaringTypeName() + '.' + joinPoint.getSignature().getName();
            LOGGER.debug("[{}]: Started exit span [{}] to execute [{}]", spanType, span.getId(), taskName);
            val result = executeJoinpoint(joinPoint);
            span.setName(taskName);
            span.setOutcome(Outcome.SUCCESS);
            return result;
        } catch (final Exception e) {
            span.setOutcome(Outcome.FAILURE);
            span.captureException(e);
            throw e;
        } finally {
            LOGGER.debug("Span [{}] ended", span.getId());
            span.end();
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
            var args = joinPoint.getArgs();
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
    private void allComponents() {
    }
}
