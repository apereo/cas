package org.apereo.cas.util;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InetAddressUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Utility")
public class InetAddressUtilsTests {

    @Test
    public void verifyOperation() {
        val host = InetAddressUtils.getCasServerHostAddress("https://github.com");
        assertNotNull(host);
    }

}
