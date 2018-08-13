package org.apereo.cas;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllWebflowTestSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Enclosed.class)
@Suite.SuiteClasses({
    WiringConfigurationTests.class,
    CasWebflowServerSessionContextConfigurationTests.class,
    CasWebflowClientSessionContextConfigurationTests.class
})
public class AllWebflowTestSuite {
}
