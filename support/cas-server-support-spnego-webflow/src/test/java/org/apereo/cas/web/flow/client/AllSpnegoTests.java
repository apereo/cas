package org.apereo.cas.web.flow.client;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllSpnegoTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({SpnegoKnownClientSystemsFilterActionTests.class,
    LdapContinuousIntegrationSpnegoKnownClientSystemsFilterActionTests.class,
    LdapSpnegoKnownClientSystemsFilterActionTests.class})
public class AllSpnegoTests {
}
