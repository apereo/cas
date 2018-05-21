package org.apereo.cas.util;

import org.apereo.cas.util.cipher.JsonWebKeySetStringCipherExecutorTests;
import org.apereo.cas.util.cipher.ProtocolTicketCipherExecutorTests;
import org.apereo.cas.util.cipher.TicketGrantingCookieCipherExecutorTests;
import org.apereo.cas.util.cipher.WebflowConversationStateCipherExecutorTests;
import org.apereo.cas.util.io.CopyServletOutputStreamTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllUtilityTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    JsonWebKeySetStringCipherExecutorTests.class,
    ProtocolTicketCipherExecutorTests.class,
    TicketGrantingCookieCipherExecutorTests.class,
    WebflowConversationStateCipherExecutorTests.class,
    ResourceUtilsTests.class,
    ScriptingUtilsTests.class,
    CopyServletOutputStreamTests.class
})
public class AllUtilityTestsSuite {
}
