
package org.apereo.cas;

import org.apereo.cas.adaptors.yubikey.JsonYubiKeyAccountRegistryTests;
import org.apereo.cas.adaptors.yubikey.RestfulYubiKeyAccountRegistryTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyAuthenticationHandlerTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyMultifactorWebflowConfigurerTests;
import org.apereo.cas.adaptors.yubikey.registry.YubiKeyAccountRegistryEndpointTests;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAuthenticationWebflowEventResolverTests;
import org.apereo.cas.config.YubiKeyAuthenticationMultifactorProviderBypassConfigurationTests;
import org.apereo.cas.config.YubiKeyConfigurationAllowedDevicesTests;
import org.apereo.cas.config.YubiKeyConfigurationOpenRegistryTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    JsonYubiKeyAccountRegistryTests.class,
    YubiKeyConfigurationAllowedDevicesTests.class,
    YubiKeyAuthenticationHandlerTests.class,
    RestfulYubiKeyAccountRegistryTests.class,
    YubiKeyAuthenticationWebflowEventResolverTests.class,
    YubiKeyAuthenticationMultifactorProviderBypassConfigurationTests.class,
    YubiKeyConfigurationOpenRegistryTests.class,
    YubiKeyAccountRegistryEndpointTests.class,
    YubiKeyMultifactorWebflowConfigurerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
