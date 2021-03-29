package org.apereo.cas.authentication;

import lombok.val;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.UncheckedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import javax.security.auth.login.AccountNotFoundException;

import java.util.Arrays;

import static org.apereo.cas.util.junit.Assertions.assertThrowsWithRootCause;
import static org.junit.Assert.assertEquals;
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
    "cas.authn.ldap[0].base-dn=dc=example,dc=org",
    "cas.authn.ldap[0].search-filter=cn={user}",
    "cas.authn.ldap[0].bind-dn=cn=Directory Manager",
    "cas.authn.ldap[0].bind-credential=password",
    "cas.authn.ldap[0].resolve-from-attribute=owner",
    "cas.authn.ldap[0].principal-attribute-list=description,cn"
    })
@EnabledIfPortOpen(port = 10389)
@Tag("Ldap")
public class DnFromAttributeAuthenticationHandlerTests extends BaseLdapAuthenticationHandlerTests {

    @Override
    String getUsername() {
        return "PD Managers";
    }

    @Override
    String getSuccessPassword() {
        return "password";
    }
}
