package org.apereo.cas.qr.web.flow;

import module java.base;
import org.apereo.cas.otp.util.QRUtils;
import org.apereo.cas.qr.QRAuthenticationConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link QRAuthenticationGenerateCodeAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class QRAuthenticationGenerateCodeAction extends BaseCasWebflowAction {
    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) throws Exception {
        val id = UUID.randomUUID().toString();
        LOGGER.debug("Generating QR code with channel id [{}]", id);
        val qrCodeBase64 = QRUtils.generateQRCode(id, QRUtils.SIZE, QRUtils.SIZE);
        val flowScope = requestContext.getFlowScope();
        flowScope.put("qrCode", qrCodeBase64);
        flowScope.put("qrChannel", id);
        flowScope.put("qrPrefix", QRAuthenticationConstants.QR_SIMPLE_BROKER_DESTINATION_PREFIX);
        return null;
    }
}
