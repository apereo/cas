package org.apereo.cas.support.saml;

import org.apereo.cas.support.saml.idp.metadata.MongoDbSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.metadata.resolver.MongoDbSamlRegisteredServiceMetadataResolverTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link MongoDbSamlTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    MongoDbSamlIdPMetadataGeneratorTests.class,
    MongoDbSamlRegisteredServiceMetadataResolverTests.class
})
public class MongoDbSamlTestsSuite {
}
