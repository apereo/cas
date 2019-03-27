package org.apereo.cas;

import org.apereo.cas.support.pac4j.authentication.DelegatedClientFactoryTests;
import org.apereo.cas.support.pac4j.authentication.handler.support.DelegatedClientAuthenticationHandlerTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */

@SelectClasses({DelegatedClientAuthenticationHandlerTests.class, DelegatedClientFactoryTests.class})
public class AllTestsSuite {
}
