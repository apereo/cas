package org.apereo.cas.support.saml.services;

import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.ClasspathResourceMetadataResolverTests;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.DynamicResourceMetadataResolverTests;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.GroovyResourceMetadataResolverTests;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.JsonResourceMetadataResolverTests;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.UrlResourceMetadataResolverTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link SamlIdPMetadataTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    SamlIdPEntityIdAuthenticationServiceSelectionStrategyTests.class,
    ClasspathResourceMetadataResolverTests.class,
    DynamicResourceMetadataResolverTests.class,
    GroovyResourceMetadataResolverTests.class,
    UrlResourceMetadataResolverTests.class,
    JsonResourceMetadataResolverTests.class
})
public class SamlIdPMetadataTestsSuite {
}
