package org.apereo.cas;

import org.apereo.cas.support.saml.idp.metadata.MongoDbSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.idp.metadata.MongoDbSamlIdPMetadataLocatorTests;
import org.apereo.cas.support.saml.metadata.resolver.MongoDbSamlRegisteredServiceMetadataResolverTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link MongoDbSamlTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    MongoDbSamlIdPMetadataGeneratorTests.class,
    MongoDbSamlIdPMetadataLocatorTests.class,
    MongoDbSamlRegisteredServiceMetadataResolverTests.class
})
@Suite
public class MongoDbSamlTestsSuite {
}
