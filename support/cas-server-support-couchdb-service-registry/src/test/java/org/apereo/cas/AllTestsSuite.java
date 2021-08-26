package org.apereo.cas;

import org.apereo.cas.couchdb.services.RegisteredServiceCouchDbRepositoryTests;
import org.apereo.cas.services.CouchDbServiceRegistryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    RegisteredServiceCouchDbRepositoryTests.class,
    CouchDbServiceRegistryTests.class
})
@Suite
public class AllTestsSuite {
}
