
package org.apereo.cas;

import org.apereo.cas.adaptors.generic.remote.RemoteAddressAuthenticationHandlerTests;
import org.apereo.cas.adaptors.generic.remote.RemoteAddressCredentialTests;
import org.apereo.cas.web.flow.RemoteAddressNonInteractiveCredentialsActionTests;
import org.apereo.cas.web.flow.RemoteAddressWebflowConfigurerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    RemoteAddressNonInteractiveCredentialsActionTests.class,
    RemoteAddressCredentialTests.class,
    RemoteAddressWebflowConfigurerTests.class,
    RemoteAddressAuthenticationHandlerTests.class
})
@Suite
public class AllTestsSuite {
}
