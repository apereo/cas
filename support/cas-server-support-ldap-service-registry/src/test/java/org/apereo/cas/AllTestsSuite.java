package org.apereo.cas;

import org.apereo.cas.adaptors.ldap.services.DefaultLdapRegisteredServiceMapperTests;
import org.apereo.cas.adaptors.ldap.services.LdapServiceRegistryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    DefaultLdapRegisteredServiceMapperTests.class,
    LdapServiceRegistryTests.class
})
@Suite
public class AllTestsSuite {
}
