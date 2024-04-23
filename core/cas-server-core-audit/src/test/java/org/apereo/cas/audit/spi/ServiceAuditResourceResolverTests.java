package org.apereo.cas.audit.spi;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ServiceAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Audits")
class ServiceAuditResourceResolverTests {

    @Nested
    @SpringBootTest(classes = BaseAuditConfigurationTests.SharedTestConfiguration.class)
    class DefaultTests {
        @Autowired
        @Qualifier("serviceAuditResourceResolver")
        private AuditResourceResolver serviceAuditResourceResolver;

        @Test
        void verifyOperation() throws Throwable {
            val jp = mock(JoinPoint.class);
            when(jp.getArgs()).thenReturn(new Object[]{"something", RegisteredServiceTestUtils.getService()});
            var input = serviceAuditResourceResolver.resolveFrom(jp, new Object());
            assertTrue(input.length > 0);
            input = serviceAuditResourceResolver.resolveFrom(jp, new RuntimeException());
            assertTrue(input.length > 0);
        }
    }

    @Nested
    @SpringBootTest(classes = BaseAuditConfigurationTests.SharedTestConfiguration.class,
        properties = "cas.audit.engine.audit-format=JSON")
    class JsonTests {
        @Autowired
        @Qualifier("serviceAuditResourceResolver")
        private AuditResourceResolver serviceAuditResourceResolver;

        @Test
        void verifyJsonOperation() throws Throwable {
            val jp = mock(JoinPoint.class);
            when(jp.getArgs()).thenReturn(new Object[]{"something", RegisteredServiceTestUtils.getService()});
            var input = serviceAuditResourceResolver.resolveFrom(jp, new Object());
            assertTrue(input.length > 0);

            input = serviceAuditResourceResolver.resolveFrom(jp, new RuntimeException());
            assertTrue(input.length > 0);
        }
    }
}
