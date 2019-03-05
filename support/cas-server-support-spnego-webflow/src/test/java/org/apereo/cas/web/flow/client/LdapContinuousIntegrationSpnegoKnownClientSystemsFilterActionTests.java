package org.apereo.cas.web.flow.client;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

import org.junit.jupiter.api.Tag;

/**
 * Test cases for {@link LdapSpnegoKnownClientSystemsFilterAction}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("Ldap")
@EnabledIfContinuousIntegration
public class LdapContinuousIntegrationSpnegoKnownClientSystemsFilterActionTests
    extends BaseLdapSpnegoKnownClientSystemsFilterActionTests {
}
