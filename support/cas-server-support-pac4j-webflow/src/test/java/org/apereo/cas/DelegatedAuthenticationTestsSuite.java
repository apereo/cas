package org.apereo.cas;

import org.apereo.cas.web.DelegatedAuthenticationWebApplicationServiceFactoryTests;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationActionTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link DelegatedAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    DelegatedAuthenticationWebApplicationServiceFactoryTests.class,
    DelegatedClientAuthenticationActionTests.class
})
@RunWith(JUnitPlatform.class)
public class DelegatedAuthenticationTestsSuite {
}
