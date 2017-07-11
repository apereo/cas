package org.apereo.cas.otp.web.flow.rest;

import org.apereo.cas.util.QRUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OneTimeTokenQRGeneratorController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RestController
public class OneTimeTokenQRGeneratorController {

    /**
     * Generate.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @GetMapping(path = {"/otp/qrgen"})
    public void generate(final HttpServletResponse response, final HttpServletRequest request) throws Exception {
        response.setContentType("image/png");
        final String key = request.getParameter("key");
        QRUtils.generateQRCode(response.getOutputStream(), key, QRUtils.WIDTH_LARGE, QRUtils.WIDTH_LARGE);
    }

}
