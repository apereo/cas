package org.apereo.cas.qr.web.flow;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

@Slf4j
public class QRLoginGenerateCodeAction extends AbstractAction {
    @Override
    protected Event doExecute(final RequestContext requestContext) {

	LOGGER.debug("~!~ I'm in the QR Login action");
	var flowScope = requestContext.getFlowScope();

	flowScope.put("qrMessage", "Look at me! I'm a message!");

	/*
        val id = UUID.randomUUID().toString();
        LOGGER.debug("Generating QR code with channel id [{}]", id);
        val qrCodeBase64 = QRUtils.generateQRCode(id, QRUtils.WIDTH_LARGE, QRUtils.WIDTH_LARGE);
        val flowScope = requestContext.getFlowScope();
        flowScope.put("qrCode", qrCodeBase64);
        flowScope.put("qrChannel", id);
        flowScope.put("qrPrefix", QRAuthenticationConstants.QR_SIMPLE_BROKER_DESTINATION_PREFIX);
	*/
        return null;
    }
}
