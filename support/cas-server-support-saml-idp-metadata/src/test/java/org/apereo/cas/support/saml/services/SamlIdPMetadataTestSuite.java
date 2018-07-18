package org.apereo.cas.support.saml.services;

import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.ClasspathResourceMetadataResolverTests;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.DynamicResourceMetadataResolverTests;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.GroovyResourceMetadataResolverTests;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.UrlResourceMetadataResolverTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link SamlIdPMetadataTestSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SamlIdPEntityIdAuthenticationServiceSelectionStrategyTests.class,
    ClasspathResourceMetadataResolverTests.class,
    DynamicResourceMetadataResolverTests.class,
    GroovyResourceMetadataResolverTests.class,
    UrlResourceMetadataResolverTests.class
})
public class SamlIdPMetadataTestSuite {
}
