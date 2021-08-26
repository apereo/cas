
package org.apereo.cas;

import org.apereo.cas.web.flow.CheckConsentRequiredActionTests;
import org.apereo.cas.web.flow.ConfirmConsentActionTests;
import org.apereo.cas.web.flow.ConsentWebflowConfigurerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    ConfirmConsentActionTests.class,
    ConsentWebflowConfigurerTests.class,
    CheckConsentRequiredActionTests.class
})
@Suite
public class AllTestsSuite {
}
