package org.apereo.cas.authentication;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit test for {@link LdapAuthenticationHandler}.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Tag("Ldap")
public class CustomPasswordPolicyLdapAuthenticationHandlerTests {
    @TestPropertySource(properties = {
        "cas.authn.ldap[0].password-policy.enabled=true",
        "cas.authn.ldap[0].password-policy.custom-policy-class=org.apereo.cas.authentication.TestAuthenticationResponseHandler"
    })
    @EnabledIfPortOpen(port = 10389)
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class ValidPasswordPolicyClassTests extends DirectLdapAuthenticationHandlerTests {
        @Test
        public void verifyOperation() {
            assertNotNull(ldapAuthenticationHandlers);
            val handler = (LdapAuthenticationHandler) ldapAuthenticationHandlers.toList().iterator().next();
            assertTrue(Arrays.stream(handler.getAuthenticator()
                .getResponseHandlers()).anyMatch(r -> r.getClass().equals(TestAuthenticationResponseHandler.class)));
        }
    }

    @TestPropertySource(properties = {
        "cas.authn.ldap[0].password-policy.enabled=true",
        "cas.authn.ldap[0].password-policy.custom-policy-class=org.apereo.cas.authentication.UnknownAuthenticationResponseHandler"
    })
    @EnabledIfPortOpen(port = 10389)
    @SuppressWarnings("ClassCanBeStatic")
    @Nested
    public class UnknownPasswordPolicyClassTests extends DirectLdapAuthenticationHandlerTests {
        @Test
        public void verifyOperation() {
            assertNotNull(ldapAuthenticationHandlers);
            val handler = (LdapAuthenticationHandler) ldapAuthenticationHandlers.toList().iterator().next();
            assertTrue(Arrays.stream(handler.getAuthenticator()
                .getResponseHandlers()).noneMatch(r -> r.getClass().equals(TestAuthenticationResponseHandler.class)));
        }
    }
}
