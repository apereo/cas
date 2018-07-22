package org.apereo.cas;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllWebflowTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    WiringConfigurationTests.class,
    CasWebflowServerSessionContextConfigurationTests.class,
    CasWebflowClientSessionContextConfigurationTests.class
})
public class AllWebflowTests {
}
