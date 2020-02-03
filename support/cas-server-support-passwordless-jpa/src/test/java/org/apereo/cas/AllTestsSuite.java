package org.apereo.cas;

import org.apereo.cas.impl.token.JpaPasswordlessTokenRepositoryTests;
import org.apereo.cas.impl.token.MySQLJpaPasswordlessTokenRepositoryTests;

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
    JpaPasswordlessTokenRepositoryTests.class,
    MySQLJpaPasswordlessTokenRepositoryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
