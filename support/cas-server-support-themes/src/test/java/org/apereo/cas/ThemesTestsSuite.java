package org.apereo.cas;

import org.apereo.cas.services.web.AggregateCasThemeSourceTests;
import org.apereo.cas.services.web.DefaultCasThemeSourceTests;
import org.apereo.cas.services.web.RegisteredServiceThemeResolverTests;
import org.apereo.cas.services.web.ServiceThemeResolverTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link ThemesTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@SelectClasses({
    DefaultCasThemeSourceTests.class,
    AggregateCasThemeSourceTests.class,
    RegisteredServiceThemeResolverTests.class,
    ServiceThemeResolverTests.class
})
@Suite
public class ThemesTestsSuite {
}
