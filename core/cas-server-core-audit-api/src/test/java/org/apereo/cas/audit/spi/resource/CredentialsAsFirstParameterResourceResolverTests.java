package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationTransaction;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.audit.AuditEngineProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;

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
        val props = new CasConfigurationProperties();
        val resolver = new CredentialsAsFirstParameterResourceResolver(props);
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword()});
        assertNotNull(resolver.resolveFrom(jp, new Object()));
    }

    @Test
    public void verifyJsonCredential() {
        val props = new CasConfigurationProperties();
        props.getAudit().getEngine().setAuditFormat(AuditEngineProperties.AuditFormatTypes.JSON);
        val resolver = new CredentialsAsFirstParameterResourceResolver(props);
        val jp = mock(JoinPoint.class);
        val cred = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "p@ssword");
        when(jp.getArgs()).thenReturn(new Object[]{cred});
        assertNotNull(resolver.resolveFrom(jp, new Object()));
    }

    @Test
    public void verifyException() {
        val props = new CasConfigurationProperties();
        val resolver = new CredentialsAsFirstParameterResourceResolver(props);
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword()});
        assertNotNull(resolver.resolveFrom(jp, new AuthenticationException()));
    }

    @Test
    public void verifyTransaction() {
        val props = new CasConfigurationProperties();
        val resolver = new CredentialsAsFirstParameterResourceResolver(props);
        val jp = mock(JoinPoint.class);
        when(jp.getArgs())
            .thenReturn(new Object[]{DefaultAuthenticationTransaction.of(
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"))});
        assertNotNull(resolver.resolveFrom(jp, new Object()));
    }
}
