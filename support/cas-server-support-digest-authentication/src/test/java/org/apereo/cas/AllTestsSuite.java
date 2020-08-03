
package org.apereo.cas;

import org.apereo.cas.config.DigestAuthenticationConfigurationTests;
import org.apereo.cas.digest.DefaultDigestHashedCredentialRetrieverTests;
import org.apereo.cas.digest.DigestCredentialTests;
import org.apereo.cas.digest.util.DigestAuthenticationUtilsTests;
import org.apereo.cas.digest.web.flow.DigestAuthenticationActionTests;
import org.apereo.cas.digest.web.flow.DigestAuthenticationWebflowConfigurerTests;

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
    DigestAuthenticationUtilsTests.class,
    DefaultDigestHashedCredentialRetrieverTests.class,
    DigestCredentialTests.class,
    DigestAuthenticationActionTests.class,
    DigestAuthenticationConfigurationTests.class,
    DigestAuthenticationWebflowConfigurerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
