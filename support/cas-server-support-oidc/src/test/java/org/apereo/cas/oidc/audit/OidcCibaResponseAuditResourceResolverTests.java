package org.apereo.cas.oidc.audit;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.web.controllers.ciba.OidcCibaResponse;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcCibaResponseAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OIDC")
class OidcCibaResponseAuditResourceResolverTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcCibaResponseResourceResolver")
    private AuditResourceResolver oidcCibaResponseResourceResolver;

    @Test
    void verifyOperation() {
        val jp = mock(JoinPoint.class);
        val responseEntity = ResponseEntity.ok().body(new OidcCibaResponse(UUID.randomUUID().toString(), 1));
        val results = oidcCibaResponseResourceResolver.resolveFrom(jp, responseEntity);
        assertNotEquals(0, results.length);
    }
}
