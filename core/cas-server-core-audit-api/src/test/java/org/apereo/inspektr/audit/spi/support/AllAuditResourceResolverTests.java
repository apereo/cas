package org.apereo.inspektr.audit.spi.support;

import org.apereo.inspektr.audit.AuditTrailManager;
import lombok.val;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceDefinition;
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
        assertNull(resolver.resolveTarget(jp, returnValue));
        val provider = new LocalAttributeMap<Object>("org.apereo.cas.authentication.MultifactorAuthenticationProvider", "provider");
        assertEquals("mfa-duo", resolver.resolveTarget(jp, new Event(this, "mfa-duo", provider)));
        assertEquals("mfa-duo", resolver.resolveTarget(jp, List.of(new Event(this, "mfa-duo", provider))));
        assertNull(resolver.resolveTarget(jp, (Object) null));
        assertNull(resolver.resolveTarget(jp, "text"));
    }

    @ParameterizedTest
    @EnumSource(AuditTrailManager.AuditFormats.class)
    void verifyApplicationTarget(final AuditTrailManager.AuditFormats format) {
        val resolver = new ShortenedReturnValueAsStringAuditResourceResolver();
        resolver.setAuditFormat(format);
        val saved = mock(RegisteredService.class);
        when(saved.getId()).thenReturn(1001L);
        val unsaved = mock(RegisteredService.class);
        when(unsaved.getId()).thenReturn(RegisteredServiceDefinition.INITIAL_IDENTIFIER_VALUE);
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{saved});
        assertEquals("1001", resolver.resolveTarget(jp, saved));
        assertNull(resolver.resolveTarget(jp, unsaved));
        assertEquals("1001", resolver.resolveTarget(jp, new IllegalArgumentException("failed")));
        when(jp.getArgs()).thenReturn(new Object[]{unsaved});
        assertNull(resolver.resolveTarget(jp, new IllegalArgumentException("failed")));
        when(jp.getArgs()).thenReturn(new Object[]{1001L});
        assertNull(resolver.resolveTarget(jp, new IllegalArgumentException("failed")));
    }
}
