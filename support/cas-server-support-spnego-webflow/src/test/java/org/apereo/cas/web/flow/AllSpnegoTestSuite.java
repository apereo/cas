package org.apereo.cas.web.flow;

import org.apereo.cas.web.flow.client.LdapContinuousIntegrationSpnegoKnownClientSystemsFilterActionTests;
import org.apereo.cas.web.flow.client.LdapSpnegoKnownClientSystemsFilterActionTests;
import org.apereo.cas.web.flow.client.SpnegoKnownClientSystemsFilterActionTests;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllSpnegoTestSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Enclosed.class)
@Suite.SuiteClasses({
    SpnegoKnownClientSystemsFilterActionTests.class,
    LdapContinuousIntegrationSpnegoKnownClientSystemsFilterActionTests.class,
    LdapSpnegoKnownClientSystemsFilterActionTests.class,
    SpnegoNegotiateCredentialsActionTests.class,
    SpnegoCredentialsActionTests.class
})
public class AllSpnegoTestSuite {
}
