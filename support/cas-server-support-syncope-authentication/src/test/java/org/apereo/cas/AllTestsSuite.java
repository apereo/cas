package org.apereo.cas;

import org.apereo.cas.syncope.SyncopeAccountRegistrationProvisionerTests;
import org.apereo.cas.syncope.authentication.SyncopeAuthenticationHandlerTests;
import org.apereo.cas.syncope.authentication.SyncopePersonAttributeDaoTests;
import org.apereo.cas.syncope.web.flow.SyncopeWebflowConfigurerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    SyncopeWebflowConfigurerTests.class,
    SyncopeAuthenticationHandlerTests.class,
    SyncopePersonAttributeDaoTests.class,
    SyncopeAccountRegistrationProvisionerTests.class
})
@Suite
public class AllTestsSuite {
}
