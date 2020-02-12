package org.apereo.cas.web.flow;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
    DefaultOpenIdUserNameExtractorTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
