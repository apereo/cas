package org.apereo.cas.qr.web.flow;

import org.apereo.cas.otp.util.QRUtils;
import org.apereo.cas.qr.QRAuthenticationConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.UUID;

/**
 * This is {@link QRAuthenticationGenerateCodeAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class QRAuthenticationGenerateCodeAction extends AbstractAction {
    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val id = UUID.randomUUID().toString();
        LOGGER.debug("Generating QR code with channel id [{}]", id);
        val qrCodeBase64 = QRUtils.generateQRCode(id, QRUtils.WIDTH_LARGE, QRUtils.WIDTH_LARGE);
        val flowScope = requestContext.getFlowScope();
        flowScope.put("qrCode", qrCodeBase64);
        flowScope.put("qrChannel", id);
        flowScope.put("qrPrefix", QRAuthenticationConstants.QR_SIMPLE_BROKER_DESTINATION_PREFIX);
        return null;
    }
}
