package org.apereo.cas.interrupt;

import org.apereo.cas.interrupt.webflow.actions.BaseInterruptFlowActionTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.crypto.PropertyBoundCipherExecutor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InterruptTrackingCookieCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Cipher")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseInterruptFlowActionTests.SharedTestConfiguration.class)
class InterruptTrackingCookieCipherExecutorTests {
    @Autowired
    @Qualifier("interruptCookieCipherExecutor")
    private PropertyBoundCipherExecutor interruptCookieCipherExecutor;

    @Test
    void verifyAction() {
        val encoded = interruptCookieCipherExecutor.encode("ST-1234567890");
        assertEquals("ST-1234567890", interruptCookieCipherExecutor.decode(encoded));
        assertNotNull(interruptCookieCipherExecutor.getName());
        assertNotNull(interruptCookieCipherExecutor.getSigningKeySetting());
        assertNotNull(interruptCookieCipherExecutor.getEncryptionKeySetting());
    }
}
