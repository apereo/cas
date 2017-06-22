package org.apereo.cas.sqrl;

import com.github.dbadia.sqrl.server.SqrlAuthPageData;
import com.github.dbadia.sqrl.server.SqrlConfig;
import com.github.dbadia.sqrl.server.SqrlServerOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;

/**
 * This is {@link SqrlGenerateQRController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Controller("sqrlGenerateQRController")
public class SqrlGenerateQRController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqrlGenerateQRController.class);

    private final SqrlConfig sqrlConfig;
    private final SqrlServerOperations sqrlServerOperations;

    public SqrlGenerateQRController(final SqrlConfig sqrlConfig, final SqrlServerOperations sqrlServerOperations) {
        this.sqrlConfig = sqrlConfig;
        this.sqrlServerOperations = sqrlServerOperations;
    }

    /**
     * Generate.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @GetMapping(path = {"/sqrl/qrgen"})
    public void generate(final HttpServletResponse response, final HttpServletRequest request) throws Exception {
        final SqrlAuthPageData pageData = sqrlServerOperations.prepareSqrlAuthPageData(request, response,
                InetAddress.getByName(request.getRemoteAddr()), 180);

        try (ByteArrayOutputStream baos = pageData.getQrCodeOutputStream()) {
            baos.flush();
            final byte[] imageInByteArray = baos.toByteArray();
            final BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageInByteArray));
            ImageIO.write(image, "png", response.getOutputStream());

            final int pageRefreshSeconds = sqrlConfig.getNutValidityInSeconds() / 2;

            final ServletContext ctx = request.getServletContext();
            ctx.setAttribute("sqrlRefreshSeconds", Integer.toString(pageRefreshSeconds));
            ctx.setAttribute("sqrlUrl", pageData.getUrl());
            ctx.setAttribute("sqrlCorrelator", pageData.getCorrelator());
        }
    }
}
