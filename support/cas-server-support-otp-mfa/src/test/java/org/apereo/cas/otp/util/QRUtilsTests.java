package org.apereo.cas.otp.util;

import org.apereo.cas.util.EncodingUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link QRUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Simple")
public class QRUtilsTests {
    @Test
    public void verifyOperation() throws Exception {
        try (val out = new ByteArrayOutputStream()) {
            val result = QRUtils.generateQRCode("test", 16, 16);
            assertNotNull(EncodingUtils.decodeBase64(result));
        }
    }
}
