package org.apereo.cas.monitor;

import org.apereo.cas.util.spring.DirectObjectProvider;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.jooq.lambda.fi.util.function.CheckedSupplier;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ExecutableObserverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Metrics")
class ExecutableObserverTests {
    @Test
    void verifyOperation() throws Throwable {
        val joinPoint = getProceedingJoinPoint();
        val observer = getExecutableObserver();
        val result = ExecutableObserver.observe(new DirectObjectProvider<>(observer), joinPoint);
        assertEquals("OK", result);
    }

    private static ExecutableObserver getExecutableObserver() {
        val observer = mock(ExecutableObserver.class);
        when(observer.supply(any(), any(CheckedSupplier.class))).thenAnswer(args -> {
            val supplier = args.getArgument(1, CheckedSupplier.class);
            return supplier.get();
        });
        return observer;
    }

    private static ProceedingJoinPoint getProceedingJoinPoint() throws Throwable {
        val joinPoint = mock(ProceedingJoinPoint.class);
        val signature = mock(Signature.class);
        when(signature.getName()).thenReturn("test");
        when(signature.getDeclaringTypeName()).thenReturn("type");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.proceed(any())).thenReturn("OK");
        when(joinPoint.proceed()).thenReturn("OK");
        val staticPart = mock(JoinPoint.StaticPart.class);
        when(staticPart.toLongString()).thenReturn(UUID.randomUUID().toString());
        when(joinPoint.getStaticPart()).thenReturn(staticPart);
        return joinPoint;
    }
}
