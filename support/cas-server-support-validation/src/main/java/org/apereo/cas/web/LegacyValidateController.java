package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorTriggerSelectionStrategy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.validation.ValidationSpecification;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Misagh Moayyed
 * @since 4.2
 */
public class LegacyValidateController extends AbstractServiceValidateController {

    public LegacyValidateController(final ValidationSpecification validationSpecification, 
                                    final AuthenticationSystemSupport authenticationSystemSupport, 
                                    final ServicesManager servicesManager, 
                                    final CentralAuthenticationService centralAuthenticationService, 
                                    final ProxyHandler proxyHandler, 
                                    final ArgumentExtractor argumentExtractor, 
                                    final MultifactorTriggerSelectionStrategy multifactorTriggerSelectionStrategy, 
                                    final AuthenticationContextValidator authenticationContextValidator, 
                                    final View jsonView, 
                                    final View successView, final View failureView, 
                                    final String authnContextAttribute) {
        super(validationSpecification, authenticationSystemSupport, servicesManager, 
                centralAuthenticationService, proxyHandler, argumentExtractor, 
                multifactorTriggerSelectionStrategy, authenticationContextValidator, 
                jsonView, successView, failureView, authnContextAttribute);
    }

    /**
     * Handle model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping(path = CasProtocolConstants.ENDPOINT_VALIDATE)
    protected ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return super.handleRequestInternal(request, response);
    }
}
