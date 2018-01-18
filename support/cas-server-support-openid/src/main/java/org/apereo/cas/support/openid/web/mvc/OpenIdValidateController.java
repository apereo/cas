package org.apereo.cas.support.openid.web.mvc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorTriggerSelectionStrategy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.openid.OpenIdProtocolConstants;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.web.AbstractServiceValidateController;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.openid4java.message.ParameterList;
import org.openid4java.message.VerifyResponse;
import org.openid4java.server.ServerManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * An Openid controller that delegates to its own views on service validates.
 * This controller is part of the {@link org.apereo.cas.web.DelegatingController}.
 * 
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public class OpenIdValidateController extends AbstractServiceValidateController {
    
    private final ServerManager serverManager;

    public OpenIdValidateController(final CasProtocolValidationSpecification validationSpecification, 
                                    final AuthenticationSystemSupport authenticationSystemSupport, 
                                    final ServicesManager servicesManager, final CentralAuthenticationService centralAuthenticationService, 
                                    final ProxyHandler proxyHandler, final ArgumentExtractor argumentExtractor, 
                                    final MultifactorTriggerSelectionStrategy multifactorTriggerSelectionStrategy, 
                                    final AuthenticationContextValidator authenticationContextValidator, 
                                    final View jsonView, final View successView, 
                                    final View failureView, final String authnContextAttribute, 
                                    final ServerManager serverManager,
                                    final ServiceTicketValidationAuthorizersExecutionPlan validationAuthorizers) {
        super(CollectionUtils.wrapSet(validationSpecification), validationAuthorizers,
            authenticationSystemSupport, servicesManager, centralAuthenticationService, proxyHandler,
            successView, failureView, argumentExtractor, multifactorTriggerSelectionStrategy,
            authenticationContextValidator, jsonView, authnContextAttribute);
        this.serverManager = serverManager;
    }

    @Override
    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String openIdMode = request.getParameter(OpenIdProtocolConstants.OPENID_MODE);
        if (StringUtils.equals(openIdMode, OpenIdProtocolConstants.CHECK_AUTHENTICATION)) {

            final VerifyResponse message = (VerifyResponse)
                    this.serverManager.verify(new ParameterList(request.getParameterMap()));

            final Map<String, String> parameters = new HashMap<>();
            parameters.putAll(message.getParameterMap());

            if (message.isSignatureVerified()) {
                LOGGER.debug("Signature verification request successful.");
                return new ModelAndView(getSuccessView(), parameters);
            }
            LOGGER.debug("Signature verification request unsuccessful.");
            return new ModelAndView(getFailureView(), parameters);
        }
        // we should probably fail here(?),
        // since we only deal OpenId signature verification
        return super.handleRequestInternal(request, response);
    }
    
    @Override
    public boolean canHandle(final HttpServletRequest request, final HttpServletResponse response) {
        final String openIdMode = request.getParameter(OpenIdProtocolConstants.OPENID_MODE);
        if (StringUtils.equals(openIdMode, OpenIdProtocolConstants.CHECK_AUTHENTICATION)) {
            LOGGER.info("Handling request. openid.mode : [{}]", openIdMode);
            return true;
        }
        LOGGER.info("Cannot handle request. openid.mode : [{}]", openIdMode);
        return false;
    }
}
