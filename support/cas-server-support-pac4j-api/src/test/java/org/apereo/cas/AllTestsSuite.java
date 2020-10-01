package org.apereo.cas;

import org.apereo.cas.integration.pac4j.DistributedJEESessionStoreTests;
import org.apereo.cas.pac4j.serialization.NimbusOAuthJacksonModuleTests;

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
    DistributedJEESessionStoreTests.class,
    NimbusOAuthJacksonModuleTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
