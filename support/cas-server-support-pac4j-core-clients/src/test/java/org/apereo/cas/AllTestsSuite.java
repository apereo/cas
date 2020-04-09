package org.apereo.cas;

import org.apereo.cas.support.pac4j.authentication.ClientAuthenticationMetaDataPopulatorTests;
import org.apereo.cas.support.pac4j.authentication.DefaultDelegatedClientFactoryTests;
import org.apereo.cas.support.pac4j.authentication.RestfulDelegatedClientFactoryTests;
import org.apereo.cas.support.pac4j.authentication.handler.support.DelegatedClientAuthenticationHandlerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */

@SelectClasses({
    DelegatedClientAuthenticationHandlerTests.class,
    RestfulDelegatedClientFactoryTests.class,
    ClientAuthenticationMetaDataPopulatorTests.class,
    DefaultDelegatedClientFactoryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
