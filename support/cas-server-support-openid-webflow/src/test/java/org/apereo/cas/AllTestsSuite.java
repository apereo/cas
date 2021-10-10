package org.apereo.cas;

import org.apereo.cas.web.flow.DefaultOpenIdUserNameExtractorTests;
import org.apereo.cas.web.flow.OpenIdCasWebflowLoginContextProviderTests;
import org.apereo.cas.web.flow.OpenIdSingleSignOnActionTests;
import org.apereo.cas.web.flow.OpenIdWebflowConfigurerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * The {@link AllTestsSuite} is responsible for
 * running all openid test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */

@SelectClasses({
    OpenIdSingleSignOnActionTests.class,
    OpenIdWebflowConfigurerTests.class,
    OpenIdCasWebflowLoginContextProviderTests.class,
    DefaultOpenIdUserNameExtractorTests.class
})
@Suite
public class AllTestsSuite {
}
