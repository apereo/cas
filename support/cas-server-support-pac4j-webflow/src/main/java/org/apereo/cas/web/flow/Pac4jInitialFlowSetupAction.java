package org.apereo.cas.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.flow.InitialFlowSetupAction;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class Pac4jInitialFlowSetupAction extends InitialFlowSetupAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pac4jInitialFlowSetupAction.class);

    public Pac4jInitialFlowSetupAction(final List<ArgumentExtractor> argumentExtractors,
                                  final ServicesManager servicesManager,
                                  final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionPlan,
                                  final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                  final CookieRetrievingCookieGenerator warnCookieGenerator,
                                  final CasConfigurationProperties casProperties) {

        super(argumentExtractors, servicesManager, authenticationRequestServiceSelectionPlan, ticketGrantingTicketCookieGenerator, warnCookieGenerator, casProperties);
    }

    @Override
    protected Event doExecute(final RequestContext context) {
        configureWebflowContextForService(context);
        return success();
    }
}
