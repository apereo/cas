package org.apereo.cas;

import org.apereo.cas.web.DelegatedAuthenticationWebApplicationServiceFactoryTests;
import org.apereo.cas.web.DelegatedClientNavigationControllerTests;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationActionTests;
import org.apereo.cas.web.saml2.Saml2ClientMetadataControllerTests;

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
    DelegatedClientAuthenticationActionTests.class,
    DelegatedClientNavigationControllerTests.class,
    Saml2ClientMetadataControllerTests.class
})
@RunWith(JUnitPlatform.class)
public class DelegatedAuthenticationTestsSuite {
}
