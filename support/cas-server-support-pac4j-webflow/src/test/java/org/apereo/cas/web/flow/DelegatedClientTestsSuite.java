package org.apereo.cas.web.flow;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link DelegatedClientTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    DelegatedClientAuthenticationActionTests.class
})
public class DelegatedClientTestsSuite {
}
