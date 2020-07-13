package org.apereo.cas.authentication.audit;

import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogateEligibilityVerificationAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class SurrogateEligibilityVerificationAuditResourceResolverTests {

    @Test
    public void verifyOperation() {
        val resolver = new SurrogateEligibilityVerificationAuditResourceResolver();
        val jp = mock(JoinPoint.class);
        val result = AuditableExecutionResult.builder()
            .authentication(CoreAuthenticationTestUtils.getAuthentication())
            .properties(Map.of("eligible", "true",
                "targetUserId", "casuser",
                "service", CoreAuthenticationTestUtils.getService()))
            .build();
        val outcome = resolver.resolveFrom(jp, result);
        assertTrue(outcome.length > 0);
    }

}
