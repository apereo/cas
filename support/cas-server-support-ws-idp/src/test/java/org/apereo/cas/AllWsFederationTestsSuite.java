package org.apereo.cas;

import org.apereo.cas.authentication.SecurityTokenServiceTokenFetcherTests;
import org.apereo.cas.ws.idp.web.flow.WSFederationIdentityProviderWebflowConfigurerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllWsFederationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    WSFederationIdentityProviderWebflowConfigurerTests.class,
    SecurityTokenServiceTokenFetcherTests.class
})
@RunWith(JUnitPlatform.class)
public class AllWsFederationTestsSuite {
}
