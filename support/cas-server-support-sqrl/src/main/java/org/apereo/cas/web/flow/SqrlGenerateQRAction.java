package org.apereo.cas.web.flow;

import com.github.dbadia.sqrl.server.SqrlAuthPageData;
import com.github.dbadia.sqrl.server.SqrlConfig;
import com.github.dbadia.sqrl.server.SqrlServerOperations;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;

/**
 * This is {@link SqrlGenerateQRAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SqrlGenerateQRAction extends AbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqrlGenerateQRAction.class);

    private static final int QR_CODE_SIZE_IN_PIXELS = 250;

    private final SqrlConfig sqrlConfig;
    private final SqrlServerOperations sqrlServerOperations;

    public SqrlGenerateQRAction(final SqrlConfig sqrlConfig, final SqrlServerOperations sqrlServerOperations) {
        this.sqrlConfig = sqrlConfig;
        this.sqrlServerOperations = sqrlServerOperations;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final HttpServletResponse response = WebUtils.getHttpServletResponse(requestContext);
        final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);

        final SqrlAuthPageData pageData = sqrlServerOperations.prepareSqrlAuthPageData(request, response,
                InetAddress.getByName(request.getRemoteAddr()), QR_CODE_SIZE_IN_PIXELS);

        try (ByteArrayOutputStream baos = pageData.getQrCodeOutputStream()) {
            baos.flush();
            final byte[] imageInByteArray = baos.toByteArray();
            final String b64 = new StringBuilder("data:image/")
                    .append(pageData.getHtmlFileType(sqrlConfig))
                    .append(";base64, ")
                    .append(EncodingUtils.encodeBase64(imageInByteArray))
                    .toString();
            final int pageRefreshSeconds = sqrlConfig.getNutValidityInSeconds() / 2;
            request.setAttribute("sqrlRefreshSeconds", Integer.toString(pageRefreshSeconds));
            request.setAttribute("sqrlQrImage", b64);
            request.setAttribute("sqrlUrl", pageData.getUrl());
            request.setAttribute("sqrlQrDescription", "Click or scan to login with SQRL");
            request.setAttribute("sqrlCorrelator", pageData.getCorrelator());
        }
        return null;
    }
}
