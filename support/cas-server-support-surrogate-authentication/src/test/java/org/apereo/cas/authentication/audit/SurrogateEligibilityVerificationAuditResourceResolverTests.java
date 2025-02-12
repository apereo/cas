package org.apereo.cas.authentication.audit;

import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
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
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogateEligibilityVerificationAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Audits")
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
class SurrogateEligibilityVerificationAuditResourceResolverTests {

    @Autowired
    @Qualifier("surrogateEligibilityVerificationAuditResourceResolver")
    private AuditResourceResolver surrogateEligibilityVerificationAuditResourceResolver;

    @Test
    void verifyOperation() {
        val jp = mock(JoinPoint.class);
        val result = AuditableExecutionResult.builder()
            .authentication(CoreAuthenticationTestUtils.getAuthentication())
            .properties(Map.of("eligible", "true",
                "targetUserId", "casuser",
                "service", CoreAuthenticationTestUtils.getService()))
            .build();
        val outcome = surrogateEligibilityVerificationAuditResourceResolver.resolveFrom(jp, result);
        assertTrue(outcome.length > 0);
    }

}
