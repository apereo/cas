package org.apereo.cas;

import org.apereo.cas.support.saml.services.SamlIdPEntityIdAuthenticationServiceSelectionStrategyTests;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCacheKeyTests;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceDefaultCachingMetadataResolverTests;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceMetadataResolverCacheLoaderTests;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.ClasspathResourceMetadataResolverTests;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.FileSystemResourceMetadataResolverTests;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.GroovyResourceMetadataResolverTests;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.JsonResourceMetadataResolverTests;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.MetadataQueryProtocolMetadataResolverTests;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataExpirationPolicyTests;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.UrlResourceMetadataResolverTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link SamlIdPMetadataTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    SamlIdPEntityIdAuthenticationServiceSelectionStrategyTests.class,
    ClasspathResourceMetadataResolverTests.class,
    MetadataQueryProtocolMetadataResolverTests.class,
    SamlRegisteredServiceMetadataExpirationPolicyTests.class,
    GroovyResourceMetadataResolverTests.class,
    UrlResourceMetadataResolverTests.class,
    SamlRegisteredServiceCacheKeyTests.class,
    SamlRegisteredServiceDefaultCachingMetadataResolverTests.class,
    SamlRegisteredServiceMetadataResolverCacheLoaderTests.class,
    FileSystemResourceMetadataResolverTests.class,
    JsonResourceMetadataResolverTests.class
})
@Suite
public class SamlIdPMetadataTestsSuite {
}
