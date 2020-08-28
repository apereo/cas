package org.apereo.cas;

import org.apereo.cas.web.flow.SpengoWebflowConfigurerTests;
import org.apereo.cas.web.flow.SpnegoCredentialsActionTests;
import org.apereo.cas.web.flow.SpnegoNegotiateCredentialsActionTests;
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
    LdapSpnegoKnownClientSystemsFilterActionTests.class,
    SpnegoNegotiateCredentialsActionTests.class,
    SpengoWebflowConfigurerTests.class,
    SpnegoCredentialsActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllSpnegoTestsSuite {
}
