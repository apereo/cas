package org.apereo.cas.audit;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.aspectj.lang.JoinPoint;
import org.junit.Test;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedAuthenticationAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DelegatedAuthenticationAuditResourceResolverTests {
    @Test
    public void verifyAction() {
        final var r = new DelegatedAuthenticationAuditResourceResolver();
        final var result = AuditableExecutionResult.of(
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getRegisteredService());
        result.addProperty(CasClient.class.getSimpleName(), new CasClient(new CasConfiguration("http://cas.example.org")));
        final var outcome = r.resolveFrom(mock(JoinPoint.class), result);
        assertTrue(outcome.length > 0);
    }
}
