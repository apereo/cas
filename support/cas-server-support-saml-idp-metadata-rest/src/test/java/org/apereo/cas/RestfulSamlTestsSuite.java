package org.apereo.cas;

import org.apereo.cas.support.saml.idp.metadata.RestfulSamlIdPMetadataCipherExecutorTests;
import org.apereo.cas.support.saml.idp.metadata.RestfulSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.idp.metadata.RestfulSamlIdPMetadataLocatorTests;
import org.apereo.cas.support.saml.metadata.resolver.RestfulSamlRegisteredServiceMetadataResolverTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link RestfulSamlTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    RestfulSamlRegisteredServiceMetadataResolverTests.class,
    RestfulSamlIdPMetadataGeneratorTests.class,
    RestfulSamlIdPMetadataCipherExecutorTests.class,
    RestfulSamlIdPMetadataLocatorTests.class
})
@RunWith(JUnitPlatform.class)
public class RestfulSamlTestsSuite {
}
