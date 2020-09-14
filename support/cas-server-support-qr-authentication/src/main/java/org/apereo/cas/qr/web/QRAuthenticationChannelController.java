package org.apereo.cas.qr.web;

import org.apereo.cas.otp.util.QRUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * This is {@link QRAuthenticationChannelController}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RestController
@Slf4j
public class QRAuthenticationChannelController {

    @GetMapping(path = "/qr/channel")
    public void generate(final HttpServletResponse response, final HttpServletRequest request) throws Exception {
        response.setContentType("image/png");
        val id = UUID.randomUUID().toString();
        LOGGER.debug("Generating QR code with channel id [{}]", id);
        QRUtils.generateQRCode(response.getOutputStream(), id, QRUtils.WIDTH_LARGE, QRUtils.WIDTH_LARGE);
    }
}
