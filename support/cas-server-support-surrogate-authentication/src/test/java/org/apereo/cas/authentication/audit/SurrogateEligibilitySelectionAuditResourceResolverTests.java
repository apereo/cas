package org.apereo.cas.authentication.audit;

import org.apereo.cas.authentication.surrogate.BaseSurrogateAuthenticationServiceTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.action.EventFactorySupport;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogateEligibilitySelectionAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Audits")
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
class SurrogateEligibilitySelectionAuditResourceResolverTests {
    @Autowired
    @Qualifier("surrogateEligibilitySelectionAuditResourceResolver")
    private AuditResourceResolver surrogateEligibilitySelectionAuditResourceResolver;

    @Test
    void verifyOperation() {
        val jp = mock(JoinPoint.class);
        val result = new HashMap<>();
        result.put("key1", "value1");
        result.put("key2", "value2");
        val event = new EventFactorySupport().success(this, result);
        val outcome = surrogateEligibilitySelectionAuditResourceResolver.resolveFrom(jp, event);
        assertTrue(outcome.length > 0);
    }

}
