package org.apereo.cas;

import org.apereo.cas.support.pac4j.authentication.DelegatedClientFactoryTests;
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
    DelegatedClientFactoryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
