package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The {@link AllCoreTestsSuite} is responsible for
 * running all cas test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({CentralAuthenticationServiceImplTests.class, 
        CentralAuthenticationServiceImplWithMockitoTests.class,
        DefaultCasAttributeEncoderTests.class,
        DefaultPrincipalAttributesRepositoryTests.class,
        MultifactorAuthenticationTests.class})
@Slf4j
public class AllCoreTestsSuite {
}
