package org.apereo.cas.adaptors.gauth.web.flow.rest;

import com.google.common.base.Throwables;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

/**
 * This is {@link GoogleAuthenticatorQRGeneratorController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RestController
public class GoogleAuthenticatorQRGeneratorController {

    /**
     * Generate.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @RequestMapping(path= { "/gauth/qrgen" })
    public void generate(final HttpServletResponse response, final HttpServletRequest request) throws Exception {
        response.setContentType("image/png");
        final String key = request.getParameter("key");
        generateQRCode(response.getOutputStream(), key);
    }

    private static void generateQRCode(final OutputStream stream, final String key) {
        try {
            final Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
            hintMap.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hintMap.put(EncodeHintType.MARGIN, 2);
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            final QRCodeWriter qrCodeWriter = new QRCodeWriter();
            final BitMatrix byteMatrix = qrCodeWriter.encode(key, BarcodeFormat.QR_CODE, 250, 250, hintMap);
            final int width = byteMatrix.getWidth();
            final BufferedImage image = new BufferedImage(width, width, BufferedImage.TYPE_INT_RGB);
            image.createGraphics();

            final Graphics2D graphics = (Graphics2D) image.getGraphics();
            try {
                graphics.setColor(Color.WHITE);
                graphics.fillRect(0, 0, width, width);
                graphics.setColor(Color.BLACK);

                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < width; j++) {
                        if (byteMatrix.get(i, j)) {
                            graphics.fillRect(i, j, 1, 1);
                        }
                    }
                }
            } finally {
                graphics.dispose();
            }
            
            ImageIO.write(image, "png", stream);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
