package org.apereo.cas;

import org.apereo.cas.authentication.SecurityTokenServiceTokenFetcherTests;
import org.apereo.cas.config.CoreWsSecuritySecurityTokenServiceConfigurationTests;
import org.apereo.cas.ws.idp.metadata.WSFederationMetadataControllerTests;
import org.apereo.cas.ws.idp.metadata.WSFederationMetadataWriterTests;
import org.apereo.cas.ws.idp.web.WSFederationRequestTests;
import org.apereo.cas.ws.idp.web.flow.WSFederationIdentityProviderWebflowConfigurerTests;
import org.apereo.cas.ws.idp.web.flow.WSFederationMetadataUIActionTests;

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
    CoreWsSecuritySecurityTokenServiceConfigurationTests.class,
    WSFederationRequestTests.class,
    WSFederationMetadataControllerTests.class,
    WSFederationMetadataWriterTests.class,
    WSFederationMetadataUIActionTests.class,
    SecurityTokenServiceTokenFetcherTests.class
})
@RunWith(JUnitPlatform.class)
public class AllWsFederationTestsSuite {
}
