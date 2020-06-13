package org.apereo.cas;

import org.apereo.cas.support.saml.idp.metadata.GitSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.idp.metadata.GitSamlIdPMetadataLocatorTests;
import org.apereo.cas.support.saml.metadata.resolver.GitSamlRegisteredServiceMetadataResolverTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link GitSamlTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SelectClasses({
    GitSamlRegisteredServiceMetadataResolverTests.class,
    GitSamlIdPMetadataGeneratorTests.class,
    GitSamlIdPMetadataLocatorTests.class
})
@RunWith(JUnitPlatform.class)
public class GitSamlTestsSuite {
}
