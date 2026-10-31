package org.apereo.cas.otp.util;

import module java.base;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.web.theme.ThemeResolver;
import org.apereo.cas.web.theme.ThemeSource;
import com.google.common.primitives.Ints;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.RequestContext;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * This is {@link QRUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@UtilityClass
@Slf4j
public class QRUtils {

    /**
     * Large width size.
     */
    public static final int SIZE = 250;

    /**
     * Theme property for the size of generated QR codes, in pixels.
     */
    public static final String THEME_PROPERTY_SIZE = "cas.qr-code.size";

    /**
     * Theme property for the color of the modules of generated QR codes.
     */
    public static final String THEME_PROPERTY_FOREGROUND = "cas.qr-code.foreground";

    /**
     * Theme property for the background color of generated QR codes.
     */
    public static final String THEME_PROPERTY_BACKGROUND = "cas.qr-code.background";

    /**
     * Theme property for the width of the quiet zone around generated QR codes, in modules.
     */
    public static final String THEME_PROPERTY_MARGIN = "cas.qr-code.margin";

    private static final int DEFAULT_MARGIN = 2;

    /**
     * Generate a QR code styled by the theme properties of the theme that applies to the current request.
     *
     * @param requestContext the request context
     * @param key            the key
     * @return the output in base64
     * @throws Exception the exception
     */
    public static String generateQRCode(final RequestContext requestContext, final String key) throws Exception {
        return generateQRCode(key, QRCodeOptions.from(themeProperties(requestContext)));
    }

    /**
     * Generate qr code.
     *
     * @param key    the key
     * @param width  the width
     * @param height the height
     * @return the output in base64
     * @throws Exception the exception
     */
    public static String generateQRCode(final String key, final int width, final int height) throws Exception {
        return generateQRCode(key, new QRCodeOptions(width, height, Color.BLACK, Color.WHITE, DEFAULT_MARGIN));
    }

    /**
     * Generate qr code with the given options.
     *
     * @param key     the key
     * @param options the options
     * @return the output in base64
     * @throws Exception the exception
     */
    public static String generateQRCode(final String key, final QRCodeOptions options) throws Exception {
        val hintMap = new EnumMap<>(EncodeHintType.class);
        hintMap.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hintMap.put(EncodeHintType.MARGIN, options.margin());
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        val qrCodeWriter = new QRCodeWriter();
        val byteMatrix = qrCodeWriter.encode(key, BarcodeFormat.QR_CODE, options.width(), options.height(), hintMap);
        val byteMatrixWidth = byteMatrix.getWidth();
        val image = new BufferedImage(byteMatrixWidth, byteMatrixWidth, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        @Cleanup("dispose")
        val graphics = (Graphics2D) image.getGraphics();

        graphics.setColor(options.background());
        graphics.fillRect(0, 0, byteMatrixWidth, byteMatrixWidth);
        graphics.setColor(options.foreground());

        IntStream.range(0, byteMatrixWidth)
            .forEach(i -> IntStream.range(0, byteMatrixWidth)
                .filter(j -> byteMatrix.get(i, j))
                .forEach(j -> graphics.fillRect(i, j, 1, 1)));

        try (val out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", out);
            val bytes = out.toByteArray();
            return EncodingUtils.encodeBase64(bytes);
        }
    }

    private static Function<String, @Nullable String> themeProperties(final RequestContext requestContext) {
        try {
            val applicationContext = requestContext.getActiveFlow().getApplicationContext();
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            if (applicationContext.containsBean("themeResolver") && applicationContext.containsBean("themeSource")) {
                val themeName = applicationContext.getBean("themeResolver", ThemeResolver.class).resolveThemeName(request);
                if (StringUtils.isNotBlank(themeName)) {
                    val theme = applicationContext.getBean("themeSource", ThemeSource.class).getTheme(Objects.requireNonNull(themeName));
                    return code -> theme.getMessageSource().getMessage(code, null, null, request.getLocale());
                }
            }
        } catch (final Exception e) {
            LOGGER.debug("Unable to read QR code theme properties: [{}]", e.getMessage());
        }
        return code -> null;
    }

    /**
     * Options that control how QR codes are drawn.
     *
     * @param width      the width in pixels
     * @param height     the height in pixels
     * @param foreground the color of the modules
     * @param background the background color
     * @param margin     the quiet zone in modules
     */
    public record QRCodeOptions(int width, int height, Color foreground, Color background, int margin) {
        private static final int MIN_SIZE = 100;
        private static final int MAX_SIZE = 1000;
        private static final int MAX_MARGIN = 10;
        private static final double LUMINANCE_RED = 0.2126;
        private static final double LUMINANCE_GREEN = 0.7152;
        private static final double LUMINANCE_BLUE = 0.0722;

        /**
         * Build options from theme properties. Sizes are kept between 100 and 1000 pixels and margins between 0 and 10;
         * colors that cannot be parsed, or a foreground that is not darker than the background, fall back to black on
         * white so the code stays scannable.
         *
         * @param themeProperties the theme properties
         * @return the options
         */
        public static QRCodeOptions from(final Function<String, @Nullable String> themeProperties) {
            val sizeValue = Ints.tryParse(StringUtils.trimToEmpty(themeProperties.apply(THEME_PROPERTY_SIZE)));
            val size = sizeValue == null ? SIZE : Math.clamp(sizeValue, MIN_SIZE, MAX_SIZE);
            val marginValue = Ints.tryParse(StringUtils.trimToEmpty(themeProperties.apply(THEME_PROPERTY_MARGIN)));
            val margin = marginValue == null ? DEFAULT_MARGIN : Math.clamp(marginValue, 0, MAX_MARGIN);
            var foreground = toColor(themeProperties.apply(THEME_PROPERTY_FOREGROUND), Color.BLACK);
            var background = toColor(themeProperties.apply(THEME_PROPERTY_BACKGROUND), Color.WHITE);
            if (luminance(foreground) >= luminance(background)) {
                LOGGER.warn("QR code foreground color must be darker than its background; using black on white");
                foreground = Color.BLACK;
                background = Color.WHITE;
            }
            return new QRCodeOptions(size, size, foreground, background, margin);
        }

        private static Color toColor(final @Nullable String value, final Color defaultColor) {
            val color = StringUtils.trimToEmpty(value);
            if (color.matches("#[0-9a-fA-F]{6}")) {
                return Color.decode(color);
            }
            return defaultColor;
        }

        private static double luminance(final Color color) {
            return LUMINANCE_RED * color.getRed() + LUMINANCE_GREEN * color.getGreen() + LUMINANCE_BLUE * color.getBlue();
        }
    }
}
