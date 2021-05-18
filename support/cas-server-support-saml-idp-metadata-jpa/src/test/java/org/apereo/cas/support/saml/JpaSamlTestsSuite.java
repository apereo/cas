package org.apereo.cas.support.saml;

import org.apereo.cas.support.saml.idp.metadata.JpaSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.idp.metadata.MicrosoftSQLServerJpaSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.idp.metadata.MySQLJpaSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.idp.metadata.OracleJpaSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.idp.metadata.PostgresJpaSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.metadata.resolver.JpaSamlRegisteredServiceMetadataResolverTests;
import org.apereo.cas.support.saml.metadata.resolver.MySQLJpaSamlRegisteredServiceMetadataResolverTests;
import org.apereo.cas.support.saml.metadata.resolver.OracleJpaSamlRegisteredServiceMetadataResolverTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link JpaSamlTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    JpaSamlIdPMetadataGeneratorTests.class,
    MySQLJpaSamlRegisteredServiceMetadataResolverTests.class,
    OracleJpaSamlRegisteredServiceMetadataResolverTests.class,
    JpaSamlRegisteredServiceMetadataResolverTests.class,
    MySQLJpaSamlIdPMetadataGeneratorTests.class,
    PostgresJpaSamlIdPMetadataGeneratorTests.class,
    OracleJpaSamlIdPMetadataGeneratorTests.class,
    MicrosoftSQLServerJpaSamlIdPMetadataGeneratorTests.class
})
@Suite
public class JpaSamlTestsSuite {
}
