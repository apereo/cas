package org.apereo.cas.support.oauth.web.audit;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20CodeResponseAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class OAuth20CodeResponseAuditResourceResolverTests {
    @Test
    public void verifyAction() {
        val r = new OAuth20CodeResponseAuditResourceResolver();
        val result = new ModelAndView("dummyView", Map.of("k1", "v1"));
        assertTrue(r.resolveFrom(mock(JoinPoint.class), result).length > 0);
    }
}
