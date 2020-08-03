package org.apereo.cas.authentication.audit;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
public class SurrogateEligibilitySelectionAuditResourceResolverTests {
    @Test
    public void verifyOperation() {
        val resolver = new SurrogateEligibilitySelectionAuditResourceResolver();
        val jp = mock(JoinPoint.class);
        val result = new HashMap<>();
        result.put("key1", "value1");
        result.put("key2", "value2");
        val event = new EventFactorySupport().success(this, result);
        val outcome = resolver.resolveFrom(jp, event);
        assertTrue(outcome.length > 0);
    }

}
