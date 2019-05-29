package org.apereo.cas.web.flow;

import org.apereo.cas.web.flow.client.LdapContinuousIntegrationSpnegoKnownClientSystemsFilterActionTests;
import org.apereo.cas.web.flow.client.LdapSpnegoKnownClientSystemsFilterActionTests;
import org.apereo.cas.web.flow.client.SpnegoKnownClientSystemsFilterActionTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllSpnegoTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    SpnegoKnownClientSystemsFilterActionTests.class,
    LdapContinuousIntegrationSpnegoKnownClientSystemsFilterActionTests.class,
    LdapSpnegoKnownClientSystemsFilterActionTests.class,
    SpnegoNegotiateCredentialsActionTests.class,
    SpnegoCredentialsActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllSpnegoTestsSuite {
}
