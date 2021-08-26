package org.apereo.cas;

import org.apereo.cas.config.CoreSamlConfigurationTests;
import org.apereo.cas.support.saml.SamlUtilsTests;
import org.apereo.cas.support.saml.authentication.SamlRestServiceTicketResourceEntityResponseFactoryTests;
import org.apereo.cas.support.saml.util.NonInflatingSaml20ObjectBuilderTests;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilderTests;
import org.apereo.cas.support.saml.util.credential.BasicResourceCredentialFactoryBeanTests;
import org.apereo.cas.support.saml.util.credential.BasicX509CredentialFactoryBeanTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
    Saml10ObjectBuilderTests.class,
    NonInflatingSaml20ObjectBuilderTests.class,
    BasicResourceCredentialFactoryBeanTests.class,
    BasicX509CredentialFactoryBeanTests.class
})
@Suite
public class AllSamlTestsSuite {
}
