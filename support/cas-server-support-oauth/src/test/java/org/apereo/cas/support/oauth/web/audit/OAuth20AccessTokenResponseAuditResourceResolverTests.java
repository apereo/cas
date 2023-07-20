package org.apereo.cas.support.oauth.web.audit;

import org.apereo.cas.configuration.model.core.audit.AuditEngineProperties;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20AccessTokenResponseAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
class OAuth20AccessTokenResponseAuditResourceResolverTests {
    @Test
    void verifyAction() {
        val resolver = new OAuth20AccessTokenResponseAuditResourceResolver(new AuditEngineProperties()
            .setAuditFormat(AuditEngineProperties.AuditFormatTypes.JSON));
        val result = new ModelAndView("dummyView", Map.of("k1", "v1"));
        assertTrue(resolver.resolveFrom(mock(JoinPoint.class), result).length > 0);
    }
}
