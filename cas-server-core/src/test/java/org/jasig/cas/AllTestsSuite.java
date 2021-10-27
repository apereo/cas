package org.jasig.cas;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The {@link AllTestsSuite} is responsible for
 * running all cas test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({CentralAuthenticationServiceImplTests.class, CentralAuthenticationServiceImplWithMockitoTests.class,
        MultifactorAuthenticationTests.class})
public class AllTestsSuite {
}
