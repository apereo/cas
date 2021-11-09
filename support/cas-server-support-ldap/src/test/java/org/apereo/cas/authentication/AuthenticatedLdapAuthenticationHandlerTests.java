package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.jooq.lambda.Unchecked;
import org.jooq.lambda.UncheckedException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.LoginException;

import static org.apereo.cas.util.junit.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link LdapAuthenticationHandler}.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.ldap[0].type=AUTHENTICATED",
    "cas.authn.ldap[0].ldap-url=ldap://localhost:10389",
    "cas.authn.ldap[0].base-dn=dc=something,dc=example,dc=org|dc=example,dc=org",
    "cas.authn.ldap[0].search-filter=cn={user}",
    "cas.authn.ldap[0].bind-dn=cn=Directory Manager",
    "cas.authn.ldap[0].bind-credential=password",
    "cas.authn.ldap[0].collect-dn-attribute=true",
    "cas.authn.ldap[0].principal-attribute-list=description,cn"
})
@EnabledIfPortOpen(port = 10389)
@Tag("Ldap")
public class AuthenticatedLdapAuthenticationHandlerTests {
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class WithoutCustomPrincipalId extends BaseLdapAuthenticationHandlerTests {
        @Test
        public void verifyAuthenticateNotFound() {
            assertThrowsWithRootCause(UncheckedException.class, AccountNotFoundException.class,
                () -> ldapAuthenticationHandlers.toList()
                    .forEach(Unchecked.consumer(h -> h.authenticate(new UsernamePasswordCredential("notfound", "badpassword")))));
        }

        @Test
        public void verifyAuthenticateFailureNotFound() {
            assertNotEquals(ldapAuthenticationHandlers.size(), 0);
            assertThrowsWithRootCause(UncheckedException.class, AccountNotFoundException.class,
                () -> ldapAuthenticationHandlers.toList().forEach(Unchecked.consumer(h -> h.authenticate(new UsernamePasswordCredential("bad", "bad")))));
        }
    }

    @TestPropertySource(properties = "cas.authn.ldap[0].principal-attribute-id=unknown")
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class WithUnknownCustomPrincipalId extends BaseLdapAuthenticationHandlerTests {
    }

    @TestPropertySource(properties = {
        "cas.authn.ldap[0].principal-attribute-id=unknown",
        "cas.authn.ldap[0].allow-missing-principal-attribute-value=false"
    })
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class WithUnknownCustomPrincipalIdFailing extends BaseLdapAuthenticationHandlerTests {
        @Override
        public void verifyAuthenticateSuccess() {
            assertThrows(LoginException.class, super::verifyAuthenticateSuccess);
        }
    }
}
