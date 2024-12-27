package org.apereo.cas.authentication;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
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
@Tag("LdapAuthentication")
class CustomPasswordPolicyLdapAuthenticationHandlerTests {
    @TestPropertySource(properties = {
        "cas.authn.ldap[0].password-policy.enabled=true",
        "cas.authn.ldap[0].password-policy.custom-policy-class=org.apereo.cas.authentication.TestAuthenticationResponseHandler"
    })
    @EnabledIfListeningOnPort(port = 10389)
    @Nested
    class ValidPasswordPolicyClassTests extends DirectLdapAuthenticationHandlerTests {
        @Test
        void verifyOperation() {
            assertNotNull(ldapAuthenticationHandlers);
            val handler = (LdapAuthenticationHandler) ldapAuthenticationHandlers.toList().getFirst();
            assertTrue(Arrays.stream(handler.getAuthenticator()
                .getResponseHandlers()).anyMatch(r -> r.getClass().equals(TestAuthenticationResponseHandler.class)));
        }
    }

    @TestPropertySource(properties = {
        "cas.authn.ldap[0].password-policy.enabled=true",
        "cas.authn.ldap[0].password-policy.custom-policy-class=org.apereo.cas.authentication.UnknownAuthenticationResponseHandler"
    })
    @EnabledIfListeningOnPort(port = 10389)
    @Nested
    class UnknownPasswordPolicyClassTests extends DirectLdapAuthenticationHandlerTests {
        @Test
        void verifyOperation() {
            assertNotNull(ldapAuthenticationHandlers);
            val handler = (LdapAuthenticationHandler) ldapAuthenticationHandlers.toList().getFirst();
            assertTrue(Arrays.stream(handler.getAuthenticator()
                .getResponseHandlers()).noneMatch(r -> r.getClass().equals(TestAuthenticationResponseHandler.class)));
        }
    }
}
