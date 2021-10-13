package org.apereo.cas.support.inwebo.authentication;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InweboCredentialTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("MFAProvider")
public class InweboCredentialTests {

    @Test
    public void verifyOperation() {
        val results = new InweboCredential("user");
        results.setAlreadyAuthenticated(true);
        results.setDeviceName("DeviceName");
        results.setOtp("123456");
        assertNotNull(results.toString());
        assertNotNull(results.getLogin());
    }
}
