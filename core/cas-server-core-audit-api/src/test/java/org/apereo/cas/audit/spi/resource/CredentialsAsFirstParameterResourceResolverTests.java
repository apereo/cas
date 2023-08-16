package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.model.core.audit.AuditEngineProperties;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CredentialsAsFirstParameterResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Audits")
class CredentialsAsFirstParameterResourceResolverTests {

    @Test
    void verifyCredential() throws Throwable {
        val resolver = getResolver();
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword()});
        assertNotNull(resolver.resolveFrom(jp, new Object()));
    }

    @Test
    void verifyJsonCredential() throws Throwable {
        val resolver = getResolver();
        val jp = mock(JoinPoint.class);
        val cred = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "p@ssword");
        when(jp.getArgs()).thenReturn(new Object[]{cred});
        assertNotNull(resolver.resolveFrom(jp, new Object()));
    }

    @Test
    void verifyException() throws Throwable {
        val resolver = getResolver();
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword()});
        assertNotNull(resolver.resolveFrom(jp, new AuthenticationException()));
    }

    @Test
    void verifyTransaction() throws Throwable {
        val resolver = getResolver();
        val jp = mock(JoinPoint.class);
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
            .newTransaction(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        when(jp.getArgs()).thenReturn(new Object[]{transaction});
        assertNotNull(resolver.resolveFrom(jp, new Object()));
    }

    private static AuditResourceResolver getResolver() {
        return new CredentialsAsFirstParameterResourceResolver(new AuditEngineProperties());
    }
}
