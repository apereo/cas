
package org.apereo.cas;

import org.apereo.cas.services.GroovySurrogateRegisteredServiceAccessStrategyTests;
import org.apereo.cas.services.SurrogateRegisteredServiceAccessStrategyTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    GroovySurrogateRegisteredServiceAccessStrategyTests.class,
    SurrogateRegisteredServiceAccessStrategyTests.class
})
@Suite
public class AllTestsSuite {
}
