package org.apereo.cas.audit.spi;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CredentialsAsFirstParameterResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Audits")
@SpringBootTest(classes = BaseAuditConfigurationTests.SharedTestConfiguration.class)
class CredentialsAsFirstParameterResourceResolverTests {

    @Autowired
    @Qualifier("credentialsAsFirstParameterResourceResolver")
    private AuditResourceResolver credentialsAsFirstParameterResourceResolver;

    @Test
    void verifyCredential() throws Throwable {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword()});
        assertNotNull(credentialsAsFirstParameterResourceResolver.resolveFrom(jp, new Object()));
    }

    @Test
    void verifyJsonCredential() throws Throwable {
        val jp = mock(JoinPoint.class);
        val cred = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "p@ssword");
        when(jp.getArgs()).thenReturn(new Object[]{cred});
        assertNotNull(credentialsAsFirstParameterResourceResolver.resolveFrom(jp, new Object()));
    }

    @Test
    void verifyException() throws Throwable {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword()});
        assertNotNull(credentialsAsFirstParameterResourceResolver.resolveFrom(jp, new AuthenticationException()));
    }

    @Test
    void verifyTransaction() throws Throwable {
        val jp = mock(JoinPoint.class);
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
            .newTransaction(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        when(jp.getArgs()).thenReturn(new Object[]{transaction});
        assertNotNull(credentialsAsFirstParameterResourceResolver.resolveFrom(jp, new Object()));
    }
}
