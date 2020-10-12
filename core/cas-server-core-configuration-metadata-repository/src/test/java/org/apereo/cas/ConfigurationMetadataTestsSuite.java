package org.apereo.cas;

import org.apereo.cas.metadata.CasConfigurationMetadataRepositoryTests;
import org.apereo.cas.metadata.rest.CasConfigurationMetadataServerEndpointTests;
import org.apereo.cas.metadata.rest.ConfigurationMetadataSearchResultTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link ConfigurationMetadataTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    CasConfigurationMetadataRepositoryTests.class,
    CasConfigurationMetadataServerEndpointTests.class,
    ConfigurationMetadataSearchResultTests.class
})
@RunWith(JUnitPlatform.class)
public class ConfigurationMetadataTestsSuite {
}
