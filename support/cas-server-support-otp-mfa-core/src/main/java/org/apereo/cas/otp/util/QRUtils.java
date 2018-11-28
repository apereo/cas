package org.apereo.cas.otp.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.stream.IntStream;

/**
 * This is {@link QRUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@UtilityClass
public class QRUtils {

    /**
     * Large width size.
     */
    public static final int WIDTH_LARGE = 250;

    /**
     * Generate qr code.
     *
     * @param stream the stream
     * @param key    the key
     * @param width  the width
     * @param height the height
     */
    @SneakyThrows
    public static void generateQRCode(final OutputStream stream, final String key,
                                      final int width, final int height) {
        val hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hintMap.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hintMap.put(EncodeHintType.MARGIN, 2);
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        val qrCodeWriter = new QRCodeWriter();
        val byteMatrix = qrCodeWriter.encode(key, BarcodeFormat.QR_CODE, width, height, hintMap);
        val byteMatrixWidth = byteMatrix.getWidth();
        val image = new BufferedImage(byteMatrixWidth, byteMatrixWidth, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        @Cleanup("dispose")
        val graphics = (Graphics2D) image.getGraphics();

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, byteMatrixWidth, byteMatrixWidth);
        graphics.setColor(Color.BLACK);

        IntStream.range(0, byteMatrixWidth)
            .forEach(i -> IntStream.range(0, byteMatrixWidth)
                .filter(j -> byteMatrix.get(i, j))
                .forEach(j -> graphics.fillRect(i, j, 1, 1)));

        ImageIO.write(image, "png", stream);
    }

}
