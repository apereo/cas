
package org.apereo.cas;

import org.apereo.cas.interrupt.GroovyScriptInterruptInquirerTests;
import org.apereo.cas.interrupt.JsonResourceInterruptInquirerTests;
import org.apereo.cas.interrupt.RegexAttributeInterruptInquirerTests;
import org.apereo.cas.interrupt.RestEndpointInterruptInquirerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    RegexAttributeInterruptInquirerTests.class,
    GroovyScriptInterruptInquirerTests.class,
    JsonResourceInterruptInquirerTests.class,
    RestEndpointInterruptInquirerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
