package org.apereo.cas.support.saml;

import org.apereo.cas.support.saml.idp.metadata.MongoDbSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.idp.metadata.MongoDbSamlIdPMetadataLocatorTests;
import org.apereo.cas.support.saml.metadata.resolver.MongoDbSamlRegisteredServiceMetadataResolverTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
@RunWith(JUnitPlatform.class)
public class MongoDbSamlTestsSuite {
}
