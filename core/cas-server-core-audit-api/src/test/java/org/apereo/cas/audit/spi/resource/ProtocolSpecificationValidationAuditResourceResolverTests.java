package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.validation.Assertion;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ProtocolSpecificationValidationAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Audits")
public class ProtocolSpecificationValidationAuditResourceResolverTests {

    @Test
    public void verifyOperation() {
        val props = new CasConfigurationProperties();
        props.getAudit().getEngine().setIncludeValidationAssertion(true);

        val resolver = new ProtocolSpecificationValidationAuditResourceResolver(props);
        val assertion = mock(Assertion.class);
        when(assertion.getService()).thenReturn(RegisteredServiceTestUtils.getService());
        when(assertion.getPrimaryAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        resolver.setAuditFormat(AuditTrailManager.AuditFormats.JSON);
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{new MockHttpServletRequest(), assertion});
        val results = resolver.resolveFrom(jp, new Object());
        assertTrue(results.length > 0);
    }

    @Test
    public void verifyNoOp() {
        val props = new CasConfigurationProperties();
        props.getAudit().getEngine().setIncludeValidationAssertion(true);

        val resolver = new ProtocolSpecificationValidationAuditResourceResolver(props);
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(ArrayUtils.EMPTY_OBJECT_ARRAY);
        val results = resolver.resolveFrom(jp, new Object());
        assertEquals(0, results.length);
    }
}
