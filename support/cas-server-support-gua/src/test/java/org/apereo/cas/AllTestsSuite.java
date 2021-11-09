
package org.apereo.cas;

import org.apereo.cas.gua.impl.LdapUserGraphicalAuthenticationRepositoryTests;
import org.apereo.cas.gua.impl.StaticUserGraphicalAuthenticationRepositoryTests;
import org.apereo.cas.web.flow.AcceptUserGraphicsForAuthenticationActionTests;
import org.apereo.cas.web.flow.DisplayUserGraphicsBeforeAuthenticationActionTests;
import org.apereo.cas.web.flow.GraphicalUserAuthenticationWebflowConfigurerTests;
import org.apereo.cas.web.flow.PrepareForGraphicalAuthenticationActionTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    StaticUserGraphicalAuthenticationRepositoryTests.class,
    LdapUserGraphicalAuthenticationRepositoryTests.class,
    PrepareForGraphicalAuthenticationActionTests.class,
    AcceptUserGraphicsForAuthenticationActionTests.class,
    GraphicalUserAuthenticationWebflowConfigurerTests.class,
    DisplayUserGraphicsBeforeAuthenticationActionTests.class
})
@Suite
public class AllTestsSuite {
}
