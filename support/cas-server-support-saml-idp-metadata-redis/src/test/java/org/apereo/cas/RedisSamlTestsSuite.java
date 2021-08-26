package org.apereo.cas;

import org.apereo.cas.support.saml.ConditionalOnExpressionNegativeTests;
import org.apereo.cas.support.saml.ConditionalOnExpressionPositiveTests;
import org.apereo.cas.support.saml.idp.metadata.RedisSamlIdPMetadataGeneratorTests;
import org.apereo.cas.support.saml.idp.metadata.RedisSamlIdPMetadataLocatorTests;
import org.apereo.cas.support.saml.metadata.resolver.RedisSamlRegisteredServiceMetadataResolverTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link RedisSamlTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SelectClasses({
    RedisSamlIdPMetadataGeneratorTests.class,
    RedisSamlIdPMetadataLocatorTests.class,
    RedisSamlRegisteredServiceMetadataResolverTests.class,
    ConditionalOnExpressionPositiveTests.class,
    ConditionalOnExpressionNegativeTests.class
})
@Suite
public class RedisSamlTestsSuite {
}
