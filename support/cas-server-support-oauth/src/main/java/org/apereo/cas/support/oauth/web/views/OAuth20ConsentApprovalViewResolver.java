package org.apereo.cas.support.oauth.web.views;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.pac4j.core.context.J2EContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link OAuth20ConsentApprovalViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuth20ConsentApprovalViewResolver implements ConsentApprovalViewResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20ConsentApprovalViewResolver.class);

    /**
     * CAS settings.
     */
    protected final CasConfigurationProperties casProperties;

    public OAuth20ConsentApprovalViewResolver(final CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
    }

    @Override
    public ModelAndView resolve(final J2EContext context, final OAuthRegisteredService service) {
        final Object bypassApprovalParameter = context.getSessionAttribute(OAuth20Constants.BYPASS_APPROVAL_PROMPT);
        LOGGER.debug("Bypassing approval prompt for service [{}]: [{}]", service, bypassApprovalParameter);

        /*
         * Inbound request; approval handled already.
         */
        if (bypassApprovalParameter != null || isConsentApprovalBypassed(context, service)) {
            return new ModelAndView();
        }
        return redirectToApproveView(context, service);
    }

    /**
     * Is consent approval bypassed?
     *
     * @param context the context
     * @param service the service
     * @return true/false
     */
    protected boolean isConsentApprovalBypassed(final J2EContext context, final OAuthRegisteredService service) {
        return service.isBypassApprovalPrompt();
    }

    /**
     * Redirect to approve view model and view.
     *
     * @param ctx the ctx
     * @param svc the svc
     * @return the model and view
     */
    protected ModelAndView redirectToApproveView(final J2EContext ctx, final OAuthRegisteredService svc) {
        final String callbackUrl = ctx.getFullRequestURL();
        ctx.setSessionAttribute(OAuth20Constants.BYPASS_APPROVAL_PROMPT, Boolean.TRUE);
        LOGGER.debug("callbackUrl: [{}]", callbackUrl);

        final Map<String, Object> model = new HashMap<>();
        model.put("service", svc);
        model.put("callbackUrl", callbackUrl);
        model.put("serviceName", svc.getName());
        model.put("deniedApprovalUrl", svc.getAccessStrategy().getUnauthorizedRedirectUrl());

        prepareApprovalViewModel(model, ctx, svc);
        return getApprovalModelAndView(model);
    }

    /**
     * Gets approval model and view.
     *
     * @param model the model
     * @return the approval model and view
     */
    protected ModelAndView getApprovalModelAndView(final Map<String, Object> model) {
        return new ModelAndView(getApprovalViewName(), model);
    }

    /**
     * Gets approval view name.
     *
     * @return the approval view name
     */
    protected String getApprovalViewName() {
        return OAuth20Constants.CONFIRM_VIEW;
    }

    /**
     * Prepare approval view model.
     *
     * @param model the model
     * @param ctx   the ctx
     * @param svc   the svc
     */
    protected void prepareApprovalViewModel(final Map<String, Object> model, final J2EContext ctx, final OAuthRegisteredService svc) {
    }
}
