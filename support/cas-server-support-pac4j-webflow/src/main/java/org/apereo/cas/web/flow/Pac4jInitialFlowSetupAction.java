package org.apereo.cas.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Class to set up variables at webflow action initialisation
 *
 * @author Francis Le Coq
 * @since 5.2
 */
public class Pac4jInitialFlowSetupAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pac4jInitialFlowSetupAction.class);

    private final CasConfigurationProperties casProperties;
    private final ServicesManager servicesManager;
    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;
    private final List<ArgumentExtractor> argumentExtractors;

    public Pac4jInitialFlowSetupAction(final List<ArgumentExtractor> argumentExtractors,
                                  final ServicesManager servicesManager,
                                  final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionPlan,
                                  final CasConfigurationProperties casProperties) {
        this.argumentExtractors = argumentExtractors;
        this.servicesManager = servicesManager;
        this.authenticationRequestServiceSelectionStrategies = authenticationRequestServiceSelectionPlan;
        this.casProperties = casProperties;
    }

    @Override
    protected Event doExecute(final RequestContext context) {
        configureWebflowContextForService(context);
        return success();
    }

    private void configureWebflowContextForService(final RequestContext context) {
        final Service service = WebUtils.getService(this.argumentExtractors, context);
        if (service != null) {
            LOGGER.debug("Placing service in context scope: [{}]", service.getId());

            final Service selectedService = authenticationRequestServiceSelectionStrategies.resolveService(service);
            final RegisteredService registeredService = this.servicesManager.findServiceBy(selectedService);
            if (registeredService != null && registeredService.getAccessStrategy().isServiceAccessAllowed()) {
                LOGGER.debug("Placing registered service [{}] with id [{}] in context scope",
                        registeredService.getServiceId(),
                        registeredService.getId());
                WebUtils.putRegisteredService(context, registeredService);

                final RegisteredServiceAccessStrategy accessStrategy = registeredService.getAccessStrategy();
                if (accessStrategy.getUnauthorizedRedirectUrl() != null) {
                    LOGGER.debug("Placing registered service's unauthorized redirect url [{}] with id [{}] in context scope",
                            accessStrategy.getUnauthorizedRedirectUrl(),
                            registeredService.getServiceId());
                    WebUtils.putUnauthorizedRedirectUrl(context, accessStrategy.getUnauthorizedRedirectUrl());
                }
            }
        } else if (!casProperties.getSso().isMissingService()) {
            LOGGER.warn("No service authentication request is available at [{}]. CAS is configured to disable the flow.",
                    WebUtils.getHttpServletRequestFromExternalWebflowContext(context).getRequestURL());
            throw new NoSuchFlowExecutionException(context.getFlowExecutionContext().getKey(),
                    new UnauthorizedServiceException("screen.service.required.message", "Service is required"));
        }
        WebUtils.putService(context, service);
    }

    public ServicesManager getServicesManager() {
        return servicesManager;
    }
}
