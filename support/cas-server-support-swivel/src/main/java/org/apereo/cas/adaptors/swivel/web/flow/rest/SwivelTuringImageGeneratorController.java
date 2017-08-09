package org.apereo.cas.adaptors.swivel.web.flow.rest;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.mfa.SwivelMultifactorProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.net.URL;

/**
 * This is {@link SwivelTuringImageGeneratorController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RestController
public class SwivelTuringImageGeneratorController {
    private final SwivelMultifactorProperties swivel;

    public SwivelTuringImageGeneratorController(final SwivelMultifactorProperties swivel) {
        this.swivel = swivel;
    }

    /**
     * Generate.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @GetMapping(path = {"/swivel/turingImage"})
    public void generate(final HttpServletResponse response, final HttpServletRequest request) throws Exception {
        response.setContentType("image/png");
        final String principal = request.getParameter("principal");
        if (StringUtils.isBlank(principal)) {
            throw new IllegalArgumentException("No principal is specified in the turing image request");
        }
        generateImage(response.getOutputStream(), principal);
    }

    private void generateImage(final OutputStream stream, final String principal) {
        try {
            final String params = String.format("?username=%s&random=%s", principal, RandomUtils.nextLong(1, Long.MAX_VALUE));
            if (StringUtils.isBlank(swivel.getSwivelTuringImageUrl())) {
                throw new IllegalArgumentException("Swivel turing image url cannot be blank and must be specified");
            }
            final URL url = new URL(swivel.getSwivelTuringImageUrl().concat(params));
            final BufferedImage image = ImageIO.read(url);
            ImageIO.write(image, "png", stream);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
