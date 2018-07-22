package org.apereo.cas.support.saml;

import org.apereo.cas.support.saml.idp.metadata.JpaSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.idp.metadata.MicrosoftSQLServerJpaSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.idp.metadata.MySQLJpaSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.metadata.resolver.JpaSamlRegisteredServiceMetadataResolverTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link JpaSamlTestSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    JpaSamlIdPMetadataGeneratorTests.class,
    JpaSamlRegisteredServiceMetadataResolverTests.class,
    MySQLJpaSamlIdPMetadataGeneratorTests.class,
    MicrosoftSQLServerJpaSamlIdPMetadataGeneratorTests.class
})
public class JpaSamlTestSuite {
}
