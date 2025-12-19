package org.apereo.inspektr.audit.spi.support;

import org.apereo.inspektr.audit.AuditTrailManager;
import lombok.val;
import module java.base;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AllAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Audits")
class AllAuditResourceResolverTests extends BaseAuditResolverTests {
    @ParameterizedTest
    @EnumSource(AuditTrailManager.AuditFormats.class)
    void verifyFirstParameter(final AuditTrailManager.AuditFormats format) {
        val resolver = new FirstParameterAuditResourceResolver();
        resolver.setAuditFormat(format);

        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{"one", 1});
        verifyAuditResourceResolver(resolver, jp, false);
    }

    @ParameterizedTest
    @EnumSource(AuditTrailManager.AuditFormats.class)
    void verifyMessageBundle(final AuditTrailManager.AuditFormats format) {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val resolver = new MessageBundleAwareResourceResolver(appCtx);
        resolver.setAuditFormat(format);
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{"one", 1});
        verifyAuditResourceResolver(resolver, jp, false);
    }

    @ParameterizedTest
    @EnumSource(AuditTrailManager.AuditFormats.class)
    void verifyObjectToString(final AuditTrailManager.AuditFormats format) {
        val resolver = new ObjectToStringResourceResolver();
        resolver.setAuditFormat(format);
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{"one", 1});
        when(jp.getTarget()).thenReturn("Target");
        verifyAuditResourceResolver(resolver, jp, false);
    }

    @ParameterizedTest
    @EnumSource(AuditTrailManager.AuditFormats.class)
    void verifyParameterAsString(final AuditTrailManager.AuditFormats format) {
        val resolver = new ParametersAsStringResourceResolver();
        resolver.setAuditFormat(format);
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{"one", 1});
        verifyAuditResourceResolver(resolver, jp, false);
    }

    @ParameterizedTest
    @EnumSource(AuditTrailManager.AuditFormats.class)
    void verifyShortenedValue(final AuditTrailManager.AuditFormats format) {
        val resolver = new ShortenedReturnValueAsStringAuditResourceResolver();
        resolver.setAuditFormat(format);
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{"one", 1});
        verifyAuditResourceResolver(resolver, jp, UUID.randomUUID().toString());
    }

    @ParameterizedTest
    @EnumSource(AuditTrailManager.AuditFormats.class)
    void verifyNullableReturnValue(final AuditTrailManager.AuditFormats format) {
        val delegate = new ObjectToStringResourceResolver();
        delegate.setAuditFormat(format);
        val resolver = new NullableReturnValueAuditResourceResolver(delegate);
        resolver.setAuditFormat(format);
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{"one", 1});
        when(jp.getTarget()).thenReturn("Target");
        assertEquals(0, resolver.resolveFrom(jp, (Object) null).length);
        val returnValue = new Event(this, "success", new LocalAttributeMap<>("name", "value"));
        verifyAuditResourceResolver(resolver, jp, returnValue);
    }
}
