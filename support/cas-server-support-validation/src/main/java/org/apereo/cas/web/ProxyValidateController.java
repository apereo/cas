package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.RequestedContextValidator;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.web.support.ArgumentExtractor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Proxy validation controller.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class ProxyValidateController extends AbstractServiceValidateController {

    public ProxyValidateController(final CasProtocolValidationSpecification validationSpecification,
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
        super(CollectionUtils.wrapSet(validationSpecification), validationAuthorizers,
            authenticationSystemSupport, servicesManager, centralAuthenticationService, proxyHandler,
            argumentExtractor, requestedContextValidator, authnContextAttribute, renewEnabled,
            validationViewFactory);
    }

    /**
     * Handle model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping(path = CasProtocolConstants.ENDPOINT_PROXY_VALIDATE)
    @Override
    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return super.handleRequestInternal(request, response);
    }
}
