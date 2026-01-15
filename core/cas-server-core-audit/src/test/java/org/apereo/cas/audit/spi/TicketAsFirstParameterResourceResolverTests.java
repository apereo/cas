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
 * This is {@link TicketAsFirstParameterResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Audits")
@ExtendWith(CasTestExtension.class)
class TicketAsFirstParameterResourceResolverTests {

    @Nested
    @SpringBootTest(classes = BaseAuditConfigurationTests.SharedTestConfiguration.class)
    class DefaultTests {
        @Autowired
        @Qualifier("ticketResourceResolver")
        private AuditResourceResolver ticketResourceResolver;

        @Test
        void verifyNullOperation() {
            val jp = mock(JoinPoint.class);
            val input = ticketResourceResolver.resolveFrom(jp, null);
            assertEquals(0, input.length);
        }

        @Test
        void verifyOperation() {
            val jp = mock(JoinPoint.class);
            when(jp.getArgs()).thenReturn(new Object[]{"ST-123434"});
            val input = ticketResourceResolver.resolveFrom(jp, null);
            assertTrue(input.length > 0);
        }

    }

    @Nested
    @SpringBootTest(classes = BaseAuditConfigurationTests.SharedTestConfiguration.class,
        properties = "cas.audit.engine.audit-format=JSON")
    class JsonTests {
        @Autowired
        @Qualifier("ticketResourceResolver")
        private AuditResourceResolver ticketResourceResolver;
        
        @Test
        void verifyTicketWithService() {
            val jp = mock(JoinPoint.class);
            val args = new Object[]{"ST-123434", RegisteredServiceTestUtils.getService()};
            when(jp.getArgs()).thenReturn(args);
            val input = ticketResourceResolver.resolveFrom(jp, null);
            assertTrue(input.length > 0);
        }

        @Test
        void verifyTicketWithServiceAsJson() {
            val jp = mock(JoinPoint.class);
            val args = new Object[]{"ST-123434", RegisteredServiceTestUtils.getService()};
            when(jp.getArgs()).thenReturn(args);
            val input = ticketResourceResolver.resolveFrom(jp, null);
            assertTrue(input.length > 0);
        }
    }


}
