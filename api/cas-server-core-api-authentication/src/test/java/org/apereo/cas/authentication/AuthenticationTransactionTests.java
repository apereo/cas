package org.apereo.cas.authentication;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @since 5.3.0
 */
public class AuthenticationTransactionTests {
    @Test
    public void verifyHasCredentialOfTypeSingle() {
        final AuthenticationTransaction transaction = AuthenticationTransaction.of(new TestCredentialType1());
        assertTrue(transaction.hasCredentialOfType(BaseTestCredential.class));
        assertTrue(transaction.hasCredentialOfType(TestCredentialType1.class));
        assertFalse(transaction.hasCredentialOfType(TestCredentialType2.class));
    }

    @Test
    public void verifyHasCredentialOfTypeMultiple() {
        final AuthenticationTransaction transaction = AuthenticationTransaction
                .of(new TestCredentialType2(), new TestCredentialType1());
        assertTrue(transaction.hasCredentialOfType(BaseTestCredential.class));
        assertTrue(transaction.hasCredentialOfType(TestCredentialType1.class));
        assertTrue(transaction.hasCredentialOfType(TestCredentialType2.class));
    }

    private abstract static class BaseTestCredential implements Credential {}
    private static class TestCredentialType1 extends BaseTestCredential {
        @Override
        public String getId() {
            return null;
        }
    }
    private static class TestCredentialType2 implements Credential {
        @Override
        public String getId() {
            return null;
        }
    }
}
