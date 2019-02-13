package org.apereo.cas.support.saml;

import org.apereo.cas.support.saml.idp.metadata.AmazonS3SamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.metadata.resolver.AmazonS3SamlRegisteredServiceMetadataResolverTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AmazonS3SamlTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    AmazonS3SamlRegisteredServiceMetadataResolverTests.class,
    AmazonS3SamlIdPMetadataGeneratorTests.class
})
public class AmazonS3SamlTestsSuite {
}
