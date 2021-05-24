package org.apereo.cas;

import org.apereo.cas.authentication.CouchbaseAuthenticationHandlerTests;
import org.apereo.cas.authentication.CouchbasePersonAttributeDaoTests;
import org.apereo.cas.config.CouchbaseConfigurationTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    CouchbaseConfigurationTests.class,
    CouchbasePersonAttributeDaoTests.class,
    CouchbaseAuthenticationHandlerTests.class
})
@Suite
public class AllTestsSuite {
}
