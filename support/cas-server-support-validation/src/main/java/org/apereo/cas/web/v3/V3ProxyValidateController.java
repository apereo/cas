package org.apereo.cas.web.v3;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.RequestedContextValidator;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.web.ServiceValidationViewFactory;
import org.apereo.cas.web.support.ArgumentExtractor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Misagh Moayyed
 * @since 4.2
 */
public class V3ProxyValidateController extends V3ServiceValidateController {

    public V3ProxyValidateController(final CasProtocolValidationSpecification validationSpecification,
                                     final AuthenticationSystemSupport authenticationSystemSupport,
                                     final ServicesManager servicesManager,
                                     final CentralAuthenticationService centralAuthenticationService,
                                     final ProxyHandler proxyHandler,
                                     final ArgumentExtractor argumentExtractor,
                                     final RequestedContextValidator requestedContextValidator,
                                     final String authnContextAttribute,
                                     final ServiceTicketValidationAuthorizersExecutionPlan validationAuthorizers,
                                     final boolean renewEnabled,
                                     final ServiceValidationViewFactory validationViewFactory) {
        super(validationSpecification, authenticationSystemSupport, servicesManager,
            centralAuthenticationService, proxyHandler, argumentExtractor, requestedContextValidator,
            authnContextAttribute, validationAuthorizers, renewEnabled, validationViewFactory);
    }

    /**
     * Handle model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping(path = CasProtocolConstants.ENDPOINT_PROXY_VALIDATE_V3)
    @Override
    protected ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return super.handleRequestInternal(request, response);
    }
}
