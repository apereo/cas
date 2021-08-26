package org.apereo.cas;

import org.apereo.cas.authentication.PasswordlessTokenAuthenticationHandlerTests;
import org.apereo.cas.impl.account.GroovyPasswordlessUserAccountStoreTests;
import org.apereo.cas.impl.account.JsonPasswordlessUserAccountStoreTests;
import org.apereo.cas.impl.account.RestfulPasswordlessUserAccountStoreTests;
import org.apereo.cas.impl.account.SimplePasswordlessUserAccountStoreTests;
import org.apereo.cas.impl.token.InMemoryPasswordlessTokenRepositoryTests;
import org.apereo.cas.impl.token.RestfulPasswordlessTokenRepositoryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    JsonPasswordlessUserAccountStoreTests.class,
    RestfulPasswordlessTokenRepositoryTests.class,
    RestfulPasswordlessUserAccountStoreTests.class,
    GroovyPasswordlessUserAccountStoreTests.class,
    SimplePasswordlessUserAccountStoreTests.class,
    InMemoryPasswordlessTokenRepositoryTests.class,
    PasswordlessTokenAuthenticationHandlerTests.class
})
@Suite
public class AllTestsSuite {
}
