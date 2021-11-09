package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @since 5.3.0
 */
@Tag("Authentication")
public class AuthenticationTransactionTests {
    @Test
    public void verifyHasCredentialOfTypeSingle() {
        val transaction = new DefaultAuthenticationTransactionFactory().newTransaction(new TestCredentialType1());
        assertTrue(transaction.hasCredentialOfType(BaseTestCredential.class));
        assertTrue(transaction.hasCredentialOfType(TestCredentialType1.class));
        assertFalse(transaction.hasCredentialOfType(TestCredentialType2.class));
    }

    @Test
    public void verifyHasCredentialOfTypeMultiple() {
        val transaction = new DefaultAuthenticationTransactionFactory().newTransaction(new TestCredentialType2(), new TestCredentialType1());
        assertTrue(transaction.hasCredentialOfType(BaseTestCredential.class));
        assertTrue(transaction.hasCredentialOfType(TestCredentialType1.class));
        assertTrue(transaction.hasCredentialOfType(TestCredentialType2.class));
    }

    @Test
    public void verifyOperation() {
        val transaction = new AuthenticationTransaction() {
            private static final long serialVersionUID = -8503574003503719399L;

            @Override
            public Collection<Authentication> getAuthentications() {
                return List.of();
            }

            @Override
            public AuthenticationTransaction collect(final Collection<Authentication> authentications) {
                return this;
            }

            @Override
            public Service getService() {
                return Mockito.mock(Service.class);
            }

            @Override
            public Collection<Credential> getCredentials() {
                return List.of(new TestCredentialType1());
            }
        };
        assertNotNull(transaction.getPrimaryCredential());
        assertNotNull(transaction.getAuthentications());
        assertNotNull(transaction.collect(List.of()));
        assertTrue(transaction.hasCredentialOfType(TestCredentialType1.class));
    }

    public abstract static class BaseTestCredential implements Credential {
        private static final long serialVersionUID = -6933725969701066361L;
    }

    public static class TestCredentialType1 extends BaseTestCredential {
        private static final long serialVersionUID = -2785558255024055757L;

        @Override
        public String getId() {
            return null;
        }
    }

    public static class TestCredentialType2 implements Credential {
        private static final long serialVersionUID = -4137096818705980020L;

        @Override
        public String getId() {
            return null;
        }
    }
}
