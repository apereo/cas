package org.apereo.cas.support.saml;

import org.apereo.cas.support.saml.idp.metadata.MongoDbSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.metadata.resolver.MongoDbSamlRegisteredServiceMetadataResolverTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link MongoDbSamlTestSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    MongoDbSamlIdPMetadataGeneratorTests.class,
    MongoDbSamlRegisteredServiceMetadataResolverTests.class
})
public class MongoDbSamlTestSuite {
}
