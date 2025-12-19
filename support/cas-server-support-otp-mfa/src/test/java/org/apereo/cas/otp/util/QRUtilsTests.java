package org.apereo.cas.otp.util;

import module java.base;
import org.apereo.cas.util.EncodingUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link QRUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Simple")
class QRUtilsTests {
    @Test
    void verifyOperation() throws Throwable {
        try (val _ = new ByteArrayOutputStream()) {
            val result = QRUtils.generateQRCode("test", 16, 16);
            assertNotNull(EncodingUtils.decodeBase64(result));
        }
    }
}
