package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.ValidationAuthorizer;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * @author Misagh Moayyed
 * @since 4.2
 */
public class ServiceValidateController extends AbstractServiceValidateController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceValidateController.class);

    public ServiceValidateController(final CasProtocolValidationSpecification validationSpecification,
                                     final AuthenticationSystemSupport authenticationSystemSupport,
                                     final ServicesManager servicesManager,
                                     final CentralAuthenticationService centralAuthenticationService,
                                     final ProxyHandler proxyHandler,
                                     final ArgumentExtractor argumentExtractor,
                                     final MultifactorTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
                                     final AuthenticationContextValidator authenticationContextValidator,
                                     final View jsonView,
                                     final View successView, final View failureView,
                                     final String authnContextAttribute, final Set<ValidationAuthorizer> validationAuthorizers) {
        super(validationSpecification, authenticationSystemSupport, servicesManager,
                centralAuthenticationService, proxyHandler, argumentExtractor,
                multifactorTriggerSelectionStrategy, authenticationContextValidator,
                jsonView, successView, failureView, authnContextAttribute, validationAuthorizers);
    }

    /**
     * Handle model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping(path = CasProtocolConstants.ENDPOINT_SERVICE_VALIDATE)
    @Override
    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return super.handleRequestInternal(request, response);
    }

    @Override
    protected void prepareForTicketValidation(final HttpServletRequest request, final WebApplicationService service, final String serviceTicketId) {
        super.prepareForTicketValidation(request, service, serviceTicketId);
        LOGGER.debug("Preparing to validate ticket [{}] for service [{}] via [{}]. Do note that this validation request "
                        + "is not equipped to release principal attributes to applications. To access the authenticated "
                        + "principal along with attributes, invoke the [{}] endpoint instead.",
                CasProtocolConstants.ENDPOINT_SERVICE_VALIDATE,
                serviceTicketId, service, CasProtocolConstants.ENDPOINT_SERVICE_VALIDATE_V3);
    }
}
