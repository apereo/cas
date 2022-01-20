package org.apereo.cas;

import org.apereo.cas.syncope.authentication.SyncopeAuthenticationHandlerTests;
import org.apereo.cas.syncope.authentication.SyncopePersonAttributeDaoTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    SyncopeAuthenticationHandlerTests.class,
    SyncopePersonAttributeDaoTests.class
})
@Suite
public class AllTestsSuite {
}
