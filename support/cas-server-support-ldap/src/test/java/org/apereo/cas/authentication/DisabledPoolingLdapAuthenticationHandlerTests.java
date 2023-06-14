package org.apereo.cas.authentication;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link DisabledPoolingLdapAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@TestPropertySource(properties = "cas.authn.ldap[0].disable-pooling=true")
@EnabledIfListeningOnPort(port = 10389)
@Tag("LdapAuthentication")
class DisabledPoolingLdapAuthenticationHandlerTests extends AuthenticatedLdapAuthenticationHandlerTests {
}
