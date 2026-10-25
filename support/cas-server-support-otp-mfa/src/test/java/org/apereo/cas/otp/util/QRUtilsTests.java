package org.apereo.cas.otp.util;

import module java.base;
import org.apereo.cas.util.EncodingUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.awt.Color;
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

    @Test
    void verifyThemeOptions() throws Throwable {
        val themed = QRUtils.QRCodeOptions.from(Map.of(
            QRUtils.THEME_PROPERTY_SIZE, "320",
            QRUtils.THEME_PROPERTY_MARGIN, "4",
            QRUtils.THEME_PROPERTY_FOREGROUND, "#114377",
            QRUtils.THEME_PROPERTY_BACKGROUND, "#fff8e1")::get);
        assertEquals(320, themed.width());
        assertEquals(4, themed.margin());
        assertEquals(new Color(0x114377), themed.foreground());
        assertEquals(new Color(0xFFF8E1), themed.background());

        val image = ImageIO.read(new ByteArrayInputStream(EncodingUtils.decodeBase64(QRUtils.generateQRCode("test", themed))));
        assertEquals(320, image.getWidth());
        assertEquals(0xFFF8E1, image.getRGB(0, 0) & 0xFFFFFF);

        val clamped = QRUtils.QRCodeOptions.from(Map.of(
            QRUtils.THEME_PROPERTY_SIZE, "5000",
            QRUtils.THEME_PROPERTY_MARGIN, "-3")::get);
        assertEquals(1000, clamped.width());
        assertEquals(0, clamped.margin());

        val inverted = QRUtils.QRCodeOptions.from(Map.of(
            QRUtils.THEME_PROPERTY_FOREGROUND, "#ffffff",
            QRUtils.THEME_PROPERTY_BACKGROUND, "#000000")::get);
        assertEquals(Color.BLACK, inverted.foreground());
        assertEquals(Color.WHITE, inverted.background());

        val defaults = QRUtils.QRCodeOptions.from(Map.of(QRUtils.THEME_PROPERTY_FOREGROUND, "blue")::get);
        assertEquals(QRUtils.SIZE, defaults.width());
        assertEquals(2, defaults.margin());
        assertEquals(Color.BLACK, defaults.foreground());
    }
}
