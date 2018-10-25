package org.apereo.cas.authentication;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @since 5.3.0
 */
public class AuthenticationTransactionTests {
    @Test
    public void verifyHasCredentialOfTypeSingle() {
        val transaction = DefaultAuthenticationTransaction.of(new TestCredentialType1());
        assertTrue(transaction.hasCredentialOfType(BaseTestCredential.class));
        assertTrue(transaction.hasCredentialOfType(TestCredentialType1.class));
        assertFalse(transaction.hasCredentialOfType(TestCredentialType2.class));
    }

    @Test
    public void verifyHasCredentialOfTypeMultiple() {
        val transaction = DefaultAuthenticationTransaction.of(new TestCredentialType2(), new TestCredentialType1());
        assertTrue(transaction.hasCredentialOfType(BaseTestCredential.class));
        assertTrue(transaction.hasCredentialOfType(TestCredentialType1.class));
        assertTrue(transaction.hasCredentialOfType(TestCredentialType2.class));
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
