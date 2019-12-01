
package org.apereo.cas;

import org.apereo.cas.aup.DefaultAcceptableUsagePolicyRepositoryTests;
import org.apereo.cas.aup.GroovyAcceptableUsagePolicyRepositoryTests;
import org.apereo.cas.web.flow.AcceptableUsagePolicySubmitActionTests;
import org.apereo.cas.web.flow.AcceptableUsagePolicyVerifyActionTests;
import org.apereo.cas.web.flow.AcceptableUsagePolicyWebflowConfigurerTests;

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
    AcceptableUsagePolicySubmitActionTests.class,
    AcceptableUsagePolicyVerifyActionTests.class,
    DefaultAcceptableUsagePolicyRepositoryTests.class,
    AcceptableUsagePolicyWebflowConfigurerTests.class,
    GroovyAcceptableUsagePolicyRepositoryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
