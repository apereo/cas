package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.validation.Assertion;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
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
@SpringBootTest(classes = RefreshAutoConfiguration.class, properties = "cas.audit.engine.include-validation-assertion=true")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
class ProtocolSpecificationValidationAuditResourceResolverTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyOperation() {
        val resolver = new ProtocolSpecificationValidationAuditResourceResolver(casProperties);
        val assertion = mock(Assertion.class);
        val service = RegisteredServiceTestUtils.getService();
        when(assertion.getService()).thenReturn(service);
        when(assertion.getPrimaryAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());
        resolver.setAuditFormat(AuditTrailManager.AuditFormats.JSON);
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{new MockHttpServletRequest(), assertion});
        val results = resolver.resolveFrom(jp, new Object());
        assertTrue(results.length > 0);
    }

    @Test
    void verifyNoOp() {
        val resolver = new ProtocolSpecificationValidationAuditResourceResolver(casProperties);
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(ArrayUtils.EMPTY_OBJECT_ARRAY);
        val results = resolver.resolveFrom(jp, new Object());
        assertEquals(0, results.length);
    }
}
