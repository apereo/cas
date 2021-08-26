package org.apereo.cas;

import org.apereo.cas.adaptors.yubikey.YubiKeyAuthenticationHandlerTests;
import org.apereo.cas.adaptors.yubikey.YubiKeyMultifactorWebflowConfigurerTests;
import org.apereo.cas.adaptors.yubikey.registry.ClosedYubiKeyAccountRegistryTests;
import org.apereo.cas.adaptors.yubikey.registry.JsonYubiKeyAccountRegistryTests;
import org.apereo.cas.adaptors.yubikey.registry.OpenYubiKeyAccountRegistryTests;
import org.apereo.cas.adaptors.yubikey.registry.RestfulYubiKeyAccountRegistryTests;
import org.apereo.cas.adaptors.yubikey.registry.YubiKeyAccountRegistryEndpointTests;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAuthenticationWebflowEventResolverTests;
import org.apereo.cas.config.YubiKeyAuthenticationMultifactorProviderBypassConfigurationTests;
import org.apereo.cas.config.YubiKeyConfigurationAllowedDevicesTests;
import org.apereo.cas.config.YubiKeyConfigurationOpenRegistryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    JsonYubiKeyAccountRegistryTests.class,
    ClosedYubiKeyAccountRegistryTests.class,
    YubiKeyConfigurationAllowedDevicesTests.class,
    YubiKeyAuthenticationHandlerTests.class,
    RestfulYubiKeyAccountRegistryTests.class,
    OpenYubiKeyAccountRegistryTests.class,
    YubiKeyAuthenticationWebflowEventResolverTests.class,
    YubiKeyAuthenticationMultifactorProviderBypassConfigurationTests.class,
    YubiKeyConfigurationOpenRegistryTests.class,
    YubiKeyAccountRegistryEndpointTests.class,
    YubiKeyMultifactorWebflowConfigurerTests.class
})
@Suite
public class AllTestsSuite {
}
