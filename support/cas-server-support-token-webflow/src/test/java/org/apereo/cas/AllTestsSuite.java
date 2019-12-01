
package org.apereo.cas;

import org.apereo.cas.web.DefaultTokenRequestExtractorTests;
import org.apereo.cas.web.TokenCredentialTests;
import org.apereo.cas.web.flow.TokenAuthenticationActionTests;
import org.apereo.cas.web.flow.TokenWebflowConfigurerTests;

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
    DefaultTokenRequestExtractorTests.class,
    TokenAuthenticationActionTests.class,
    TokenWebflowConfigurerTests.class,
    TokenCredentialTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
