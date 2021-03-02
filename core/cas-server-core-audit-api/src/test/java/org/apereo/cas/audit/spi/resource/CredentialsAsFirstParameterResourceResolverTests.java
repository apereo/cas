package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionFactory;

import lombok.val;
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
public class CredentialsAsFirstParameterResourceResolverTests {

    @Test
    public void verifyCredential() {
        val resolver = new CredentialsAsFirstParameterResourceResolver();
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword()});
        assertNotNull(resolver.resolveFrom(jp, new Object()));
    }

    @Test
    public void verifyJsonCredential() {
        val resolver = new CredentialsAsFirstParameterResourceResolver();
        val jp = mock(JoinPoint.class);
        val cred = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "p@ssword");
        when(jp.getArgs()).thenReturn(new Object[]{cred});
        assertNotNull(resolver.resolveFrom(jp, new Object()));
    }

    @Test
    public void verifyException() {
        val resolver = new CredentialsAsFirstParameterResourceResolver();
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword()});
        assertNotNull(resolver.resolveFrom(jp, new AuthenticationException()));
    }

    @Test
    public void verifyTransaction() {
        val resolver = new CredentialsAsFirstParameterResourceResolver();
        val jp = mock(JoinPoint.class);
        when(jp.getArgs())
            .thenReturn(new Object[]{new DefaultAuthenticationTransactionFactory().newTransaction(
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"))});
        assertNotNull(resolver.resolveFrom(jp, new Object()));
    }
}
