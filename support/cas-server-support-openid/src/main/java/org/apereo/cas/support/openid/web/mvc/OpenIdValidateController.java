package org.apereo.cas.support.openid.web.mvc;

import org.apereo.cas.support.openid.OpenIdProtocolConstants;
import org.apereo.cas.web.AbstractServiceValidateController;
import org.apereo.cas.web.ServiceValidateConfigurationContext;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.openid4java.message.ParameterList;
import org.openid4java.message.VerifyResponse;
import org.openid4java.server.ServerManager;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * An Openid controller that delegates to its own views on service validates.
 * This controller is part of the {@link org.apereo.cas.web.DelegatingController}.
 *
 * @author Misagh Moayyed
 * @deprecated 6.2
 * @since 4.2
 */
@Slf4j
@Deprecated(since = "6.2.0")
public class OpenIdValidateController extends AbstractServiceValidateController {

    private final ServerManager serverManager;

    public OpenIdValidateController(final ServiceValidateConfigurationContext serviceValidateConfigurationContext,
                                    final ServerManager serverManager) {
        super(serviceValidateConfigurationContext);
        this.serverManager = serverManager;
    }

    @Override
    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        val openIdMode = request.getParameter(OpenIdProtocolConstants.OPENID_MODE);
        if (StringUtils.equals(openIdMode, OpenIdProtocolConstants.CHECK_AUTHENTICATION)) {

            val requestParams = new ParameterList(request.getParameterMap());
            val message = (VerifyResponse) this.serverManager.verify(requestParams);

            val parameters = new HashMap<String, String>(message.getParameterMap());
            if (message.isSignatureVerified()) {
                LOGGER.debug("Signature verification request successful.");
                return new ModelAndView(getServiceValidateConfigurationContext().getValidationViewFactory()
                    .getSuccessView(getClass()), parameters);
            }
            LOGGER.debug("Signature verification request unsuccessful.");
            return new ModelAndView(getServiceValidateConfigurationContext().getValidationViewFactory()
                .getFailureView(getClass()), parameters);
        }
        return super.handleRequestInternal(request, response);
    }

    @Override
    public boolean canHandle(final HttpServletRequest request, final HttpServletResponse response) {
        val openIdMode = request.getParameter(OpenIdProtocolConstants.OPENID_MODE);
        if (StringUtils.equals(openIdMode, OpenIdProtocolConstants.CHECK_AUTHENTICATION)) {
            LOGGER.info("Handling request. openid.mode : [{}]", openIdMode);
            return true;
        }
        LOGGER.info("Cannot handle request. openid.mode : [{}]", openIdMode);
        return false;
    }
}
