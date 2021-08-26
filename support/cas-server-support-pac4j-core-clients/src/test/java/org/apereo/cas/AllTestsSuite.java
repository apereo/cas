package org.apereo.cas;

import org.apereo.cas.support.pac4j.DelegatedClientJacksonModuleTests;
import org.apereo.cas.support.pac4j.authentication.ClientAuthenticationMetaDataPopulatorTests;
import org.apereo.cas.support.pac4j.authentication.DefaultDelegatedClientFactoryTests;
import org.apereo.cas.support.pac4j.authentication.RestfulDelegatedClientFactoryTests;
import org.apereo.cas.support.pac4j.authentication.handler.support.DelegatedClientAuthenticationHandlerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */

@SelectClasses({
    DelegatedClientAuthenticationHandlerTests.class,
    RestfulDelegatedClientFactoryTests.class,
    DelegatedClientJacksonModuleTests.class,
    ClientAuthenticationMetaDataPopulatorTests.class,
    DefaultDelegatedClientFactoryTests.class
})
@Suite
public class AllTestsSuite {
}
