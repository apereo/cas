package org.apereo.cas;

import org.apereo.cas.config.CoreSamlConfigurationTests;
import org.apereo.cas.support.saml.SamlUtilsTests;
import org.apereo.cas.support.saml.authentication.SamlRestServiceTicketResourceEntityResponseFactoryTests;
import org.apereo.cas.support.saml.util.NonInflatingSaml20ObjectBuilderTests;
import org.apereo.cas.support.saml.util.credential.BasicResourceCredentialFactoryBeanTests;
import org.apereo.cas.support.saml.util.credential.BasicX509CredentialFactoryBeanTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllSamlTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    CoreSamlConfigurationTests.class,
    SamlRestServiceTicketResourceEntityResponseFactoryTests.class,
    SamlUtilsTests.class,
    NonInflatingSaml20ObjectBuilderTests.class,
    BasicResourceCredentialFactoryBeanTests.class,
    BasicX509CredentialFactoryBeanTests.class
})
@RunWith(JUnitPlatform.class)
public class AllSamlTestsSuite {
}
