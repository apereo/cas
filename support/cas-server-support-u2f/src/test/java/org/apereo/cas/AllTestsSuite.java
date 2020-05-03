
package org.apereo.cas;

import org.apereo.cas.adaptors.u2f.U2FAuthenticationHandlerTests;
import org.apereo.cas.adaptors.u2f.storage.U2FGroovyResourceDeviceRepositoryTests;
import org.apereo.cas.adaptors.u2f.storage.U2FInMemoryDeviceRepositoryTests;
import org.apereo.cas.adaptors.u2f.storage.U2FJsonResourceDeviceRepositoryTests;
import org.apereo.cas.adaptors.u2f.storage.U2FRestResourceDeviceRepositoryTests;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAccountCheckRegistrationActionTests;
import org.apereo.cas.adaptors.u2f.web.flow.U2FAccountSaveRegistrationActionTests;
import org.apereo.cas.adaptors.u2f.web.flow.U2FStartRegistrationActionTests;

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
    U2FAccountSaveRegistrationActionTests.class,
    U2FAccountCheckRegistrationActionTests.class,
    U2FStartRegistrationActionTests.class,
    U2FRestResourceDeviceRepositoryTests.class,
    U2FGroovyResourceDeviceRepositoryTests.class,
    U2FJsonResourceDeviceRepositoryTests.class,
    U2FInMemoryDeviceRepositoryTests.class,
    U2FAuthenticationHandlerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
