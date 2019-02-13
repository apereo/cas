package org.apereo.cas.support.saml;

import org.apereo.cas.support.saml.idp.metadata.JpaSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.idp.metadata.MicrosoftSQLServerJpaSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.idp.metadata.MySQLJpaSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.metadata.resolver.JpaSamlRegisteredServiceMetadataResolverTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link JpaSamlTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    JpaSamlIdPMetadataGeneratorTests.class,
    JpaSamlRegisteredServiceMetadataResolverTests.class,
    MySQLJpaSamlIdPMetadataGeneratorTests.class,
    MicrosoftSQLServerJpaSamlIdPMetadataGeneratorTests.class
})
public class JpaSamlTestsSuite {
}
