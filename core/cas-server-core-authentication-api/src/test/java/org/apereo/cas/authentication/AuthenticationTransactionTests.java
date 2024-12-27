package org.apereo.cas.authentication;

import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
import org.apereo.cas.authentication.principal.Service;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @since 5.3.0
 */
@Tag("Authentication")
class AuthenticationTransactionTests {
    @Test
    void verifyHasCredentialOfTypeSingle() {
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(new TestCredentialType1());
        assertTrue(transaction.hasCredentialOfType(BaseTestCredential.class));
        assertTrue(transaction.hasCredentialOfType(TestCredentialType1.class));
        assertFalse(transaction.hasCredentialOfType(TestCredentialType2.class));
    }

    @Test
    void verifyHasCredentialOfTypeMultiple() {
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(new TestCredentialType2(), new TestCredentialType1());
        assertTrue(transaction.hasCredentialOfType(BaseTestCredential.class));
        assertTrue(transaction.hasCredentialOfType(TestCredentialType1.class));
        assertTrue(transaction.hasCredentialOfType(TestCredentialType2.class));
    }

    @Test
    void verifyOperation() throws Throwable {
        val transaction = new AuthenticationTransaction() {
            @Serial
            private static final long serialVersionUID = -8503574003503719399L;

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

            @Override
            public Collection<Authentication> getAuthentications() {
                return List.of();
            }
        };
        assertNotNull(transaction.getPrimaryCredential());
        assertNotNull(transaction.getAuthentications());
        assertNotNull(transaction.collect(List.of()));
        assertTrue(transaction.hasCredentialOfType(TestCredentialType1.class));
    }

    public abstract static class BaseTestCredential implements Credential {
        @Serial
        private static final long serialVersionUID = -6933725969701066361L;

        @Override
        public CredentialMetadata getCredentialMetadata() {
            return new BasicCredentialMetadata(this);
        }
    }

    static class TestCredentialType1 extends BaseTestCredential {
        @Serial
        private static final long serialVersionUID = -2785558255024055757L;

        @Override
        public String getId() {
            return null;
        }
    }

    static class TestCredentialType2 implements Credential {
        @Serial
        private static final long serialVersionUID = -4137096818705980020L;

        @Override
        public String getId() {
            return null;
        }

        @Override
        public CredentialMetadata getCredentialMetadata() {
            return new BasicCredentialMetadata(this);
        }
    }
}
