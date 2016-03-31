package org.jasig.cas.support.openid.web.mvc;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.support.openid.OpenIdProtocolConstants;
import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.validation.ValidationSpecification;
import org.jasig.cas.web.AbstractServiceValidateController;
import org.openid4java.message.ParameterList;
import org.openid4java.message.VerifyResponse;
import org.openid4java.server.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * An Openid controller that delegates to its own views on service validates.
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("openIdValidateController")
public class OpenIdValidateController extends AbstractServiceValidateController {

    private transient Logger logger = LoggerFactory.getLogger(OpenIdValidateController.class);

    @Autowired
    @Qualifier("serverManager")
    private ServerManager serverManager;

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
        final String openIdMode = request.getParameter(OpenIdProtocolConstants.OPENID_MODE);
        if (StringUtils.equals(openIdMode, OpenIdProtocolConstants.CHECK_AUTHENTICATION)) {

            final VerifyResponse message = (VerifyResponse)
                serverManager.verify(new ParameterList(request.getParameterMap()));

            final Map<String, String> parameters = new HashMap<>();
            parameters.putAll(message.getParameterMap());

            if(message.isSignatureVerified()) {
                logger.debug("Signature verification request successful.");
                return new ModelAndView(getSuccessView(), "parameters", parameters);
            } else {
                logger.debug("Signature verification request unsuccessful.");
                return new ModelAndView(getFailureView(), "parameters", parameters);
            }
        } else {
            // we should probably fail here(?),
            // since we only deal OpenId signature verification
            return super.handleRequestInternal(request, response);
        }
    }

    @Override
    @Autowired
    @Scope("prototype")
    public void setValidationSpecification(
            @Qualifier("cas20WithoutProxyProtocolValidationSpecification")
            final ValidationSpecification validationSpecification) {
        super.setValidationSpecification(validationSpecification);
    }


    @Override
    @Autowired
    public void setFailureView(@Value("casOpenIdServiceFailureView") final String failureView) {
        super.setFailureView(failureView);
    }

    @Override
    @Autowired
    public void setSuccessView(@Value("casOpenIdServiceSuccessView") final String successView) {
        super.setSuccessView(successView);
    }

    @Override
    @Autowired
    public void setProxyHandler(@Qualifier("proxy20Handler") final ProxyHandler proxyHandler) {
        super.setProxyHandler(proxyHandler);
    }

    @Override
    public boolean canHandle(final HttpServletRequest request, final HttpServletResponse response) {
        final String openIdMode = request.getParameter(OpenIdProtocolConstants.OPENID_MODE);
        if (StringUtils.equals(openIdMode, OpenIdProtocolConstants.CHECK_AUTHENTICATION)) {
            logger.info("Handling request. openid.mode : {}", openIdMode);
            return true;
        }
        logger.info("Cannot handle request. openid.mode : {}", openIdMode);
        return false;
    }
}
