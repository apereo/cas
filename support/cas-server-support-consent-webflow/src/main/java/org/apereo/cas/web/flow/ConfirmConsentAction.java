package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.services.ServicesManager;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link ConfirmConsentAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ConfirmConsentAction extends AbstractAction {
    /**
     * Indicates that webflow should proceed with consent.
     */
    public static final String EVENT_ID_CONSENT_REQUIRED = "consentRequired";

    private final ServicesManager servicesManager;
    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final ConsentRepository consentRepository;
    private final ConsentEngine consentEngine;

    public ConfirmConsentAction(final ServicesManager servicesManager,
                                final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                final AuthenticationSystemSupport authenticationSystemSupport,
                                final ConsentRepository consentRepository,
                                final ConsentEngine consentEngine) {
        this.servicesManager = servicesManager;
        this.authenticationRequestServiceSelectionStrategies = authenticationRequestServiceSelectionStrategies;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.consentRepository = consentRepository;
        this.consentEngine = consentEngine;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        return new EventFactorySupport().success(this);
    }
}
