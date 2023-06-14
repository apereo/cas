package org.apereo.cas.support.oauth.web.audit;

import org.apereo.cas.configuration.model.core.audit.AuditEngineProperties;
import org.apereo.cas.util.RandomUtils;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20AuthorizationResponseAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
class OAuth20AuthorizationResponseAuditResourceResolverTests {
    @Test
    void verifyAction() {
        val r = new OAuth20AuthorizationResponseAuditResourceResolver(
            new AuditEngineProperties().setAuditFormat(AuditEngineProperties.AuditFormatTypes.JSON));
        val modelAndView = new ModelAndView("dummyView",
            Map.of("k1", "v1", "longkey", RandomUtils.randomAlphabetic(60)));
        val result = r.resolveFrom(mock(JoinPoint.class), modelAndView);
        assertTrue(result.length > 0);
    }
}
