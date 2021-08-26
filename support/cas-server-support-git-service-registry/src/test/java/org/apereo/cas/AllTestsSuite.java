package org.apereo.cas;

import org.apereo.cas.locator.DefaultGitRepositoryRegisteredServiceLocatorTests;
import org.apereo.cas.locator.TypeAwareGitRepositoryRegisteredServiceLocatorTests;
import org.apereo.cas.services.GitServiceRegistryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    GitServiceRegistryTests.class,
    TypeAwareGitRepositoryRegisteredServiceLocatorTests.class,
    DefaultGitRepositoryRegisteredServiceLocatorTests.class
})
@Suite
public class AllTestsSuite {
}
