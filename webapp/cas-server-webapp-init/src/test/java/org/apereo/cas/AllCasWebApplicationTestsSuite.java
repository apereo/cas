package org.apereo.cas;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllCasWebApplicationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses(CasEmbeddedContainerUtilsTests.class)
@RunWith(JUnitPlatform.class)
public class AllCasWebApplicationTestsSuite {
}
