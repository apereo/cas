package org.apereo.cas;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllCasWebApplicationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses(CasEmbeddedContainerUtilsTests.class)
@Suite
public class AllCasWebApplicationTestsSuite {
}
