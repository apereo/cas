package org.apereo.cas.qr.web.flow;

import org.apereo.cas.qr.authentication.QRAuthenticationTokenCredential;
import org.apereo.cas.token.TokenConstants;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link QRAuthenticationValidateTokenAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class QRAuthenticationValidateTokenAction extends BaseCasWebflowAction {

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val token = request.getParameter(TokenConstants.PARAMETER_NAME_TOKEN);
        val deviceId = request.getParameter("deviceId");
        LOGGER.debug("Received QR token [{}] with device identifier [{}]", token, deviceId);
        val credential = new QRAuthenticationTokenCredential(token, deviceId);
        WebUtils.putCredential(requestContext, credential);
        return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_FINALIZE);
    }
}
