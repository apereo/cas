package org.apereo.cas.audit.spi;

import module java.base;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        void verifyOperation() {
            val jp = mock(JoinPoint.class);
            val args = new Object[]{"something", RegisteredServiceTestUtils.getService()};
            when(jp.getArgs()).thenReturn(args);
            var input = serviceAuditResourceResolver.resolveFrom(jp, new Object());
            assertTrue(input.length > 0);
            input = serviceAuditResourceResolver.resolveFrom(jp, new RuntimeException());
            assertTrue(input.length > 0);
        }
    }

    @Nested
    @SpringBootTest(classes = BaseAuditConfigurationTests.SharedTestConfiguration.class,
        properties = "cas.audit.engine.audit-format=JSON")
    @ExtendWith(CasTestExtension.class)
    class JsonTests {
        @Autowired
        @Qualifier("serviceAuditResourceResolver")
        private AuditResourceResolver serviceAuditResourceResolver;

        @Test
        void verifyJsonOperation() {
            val jp = mock(JoinPoint.class);
            val args = new Object[]{"something", RegisteredServiceTestUtils.getService()};
            when(jp.getArgs()).thenReturn(args);
            var input = serviceAuditResourceResolver.resolveFrom(jp, new Object());
            assertTrue(input.length > 0);

            input = serviceAuditResourceResolver.resolveFrom(jp, new RuntimeException());
            assertTrue(input.length > 0);
        }
    }
}
