package org.apereo.cas;

import org.apereo.cas.support.openid.authentication.handler.support.OpenIdCredentialsAuthenticationHandlerTests;
import org.apereo.cas.support.openid.authentication.principal.OpenIdServiceFactoryTests;
import org.apereo.cas.support.openid.authentication.principal.OpenIdServiceTests;
import org.apereo.cas.support.openid.web.DelegatingControllerTests;
import org.apereo.cas.support.openid.web.mvc.OpenIdValidateControllerTests;
import org.apereo.cas.support.openid.web.mvc.SmartOpenIdControllerTests;
import org.apereo.cas.support.openid.web.support.OpenIdPostUrlHandlerMappingTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * The {@link AllTestsSuite} is responsible for
 * running all openid test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */

@SelectClasses({
    OpenIdPostUrlHandlerMappingTests.class,
    SmartOpenIdControllerTests.class,
    DelegatingControllerTests.class,
    OpenIdCredentialsAuthenticationHandlerTests.class,
    OpenIdServiceFactoryTests.class,
    OpenIdValidateControllerTests.class,
    OpenIdServiceTests.class
})
@Suite
public class AllTestsSuite {
}
