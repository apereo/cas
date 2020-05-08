package org.apereo.cas;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author leeyc0
 * @since 6.2.0
 */
@SelectClasses(JdbcServletContextListenerTests.class)
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
