package org.apereo.cas.authentication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AuthenticationTransactionTest {
    @Test
    public void verifyHasCredentialOfTypeSingle() {
        final AuthenticationTransaction transaction = AuthenticationTransaction.wrap(new TestCredentialType1());
        assertTrue(transaction.isCredentialOfType(BaseTestCredential.class));
        assertTrue(transaction.isCredentialOfType(TestCredentialType1.class));
        assertFalse(transaction.isCredentialOfType(TestCredentialType2.class));
    }

    @Test
    public void verifyHasCredentialOfTypeMultiple() {
        final AuthenticationTransaction transaction = AuthenticationTransaction
                .wrap(new TestCredentialType2(), new TestCredentialType1());
        assertTrue(transaction.isCredentialOfType(BaseTestCredential.class));
        assertTrue(transaction.isCredentialOfType(TestCredentialType1.class));
        assertTrue(transaction.isCredentialOfType(TestCredentialType2.class));
    }

    private abstract class BaseTestCredential implements Credential {}
    private class TestCredentialType1 extends BaseTestCredential {
        @Override
        public String getId() {
            return null;
        }
    }
    private class TestCredentialType2 implements Credential {
        @Override
        public String getId() {
            return null;
        }
    }
}
