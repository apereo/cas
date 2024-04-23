package org.apereo.cas.support.oauth.web.views;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link OAuth20ConsentApprovalViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20ConsentApprovalViewResolver implements ConsentApprovalViewResolver {

    /**
     * CAS settings.
     */
    protected final CasConfigurationProperties casProperties;

    /**
     * Session store reference.
     */
    protected final SessionStore sessionStore;

    @Override
    public ModelAndView resolve(final WebContext context, final OAuthRegisteredService service) throws Exception {
        var bypassApprovalParameter = context.getRequestParameter(OAuth20Constants.BYPASS_APPROVAL_PROMPT)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        if (StringUtils.isBlank(bypassApprovalParameter)) {
            bypassApprovalParameter = sessionStore
                .get(context, OAuth20Constants.BYPASS_APPROVAL_PROMPT)
                .map(String::valueOf).orElse(StringUtils.EMPTY);
        }
        LOGGER.trace("Bypassing approval prompt for service [{}]: [{}]", service, bypassApprovalParameter);
        if (Boolean.TRUE.toString().equalsIgnoreCase(bypassApprovalParameter) || isConsentApprovalBypassed(context, service)) {
            sessionStore.set(context, OAuth20Constants.BYPASS_APPROVAL_PROMPT, Boolean.TRUE.toString());
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
    protected boolean isConsentApprovalBypassed(final WebContext context, final OAuthRegisteredService service) {
        return service.isBypassApprovalPrompt()
               || casProperties.getAuthn().getOauth().getCore().isBypassApprovalPrompt();
    }

    /**
     * Redirect to approve view model and view.
     *
     * @param ctx the ctx
     * @param svc the svc
     * @return the model and view
     * @throws Exception the exception
     */
    protected ModelAndView redirectToApproveView(final WebContext ctx,
                                                 final OAuthRegisteredService svc) throws Exception {
        val callbackUrl = ctx.getFullRequestURL();
        LOGGER.trace("callbackUrl: [{}]", callbackUrl);

        val url = new URIBuilder(callbackUrl);
        url.addParameter(OAuth20Constants.BYPASS_APPROVAL_PROMPT, Boolean.TRUE.toString());
        val model = new HashMap<String, Object>();
        model.put("service", svc);
        model.put("callbackUrl", url.toString());
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
     * @throws Exception the exception
     */
    protected void prepareApprovalViewModel(final Map<String, Object> model, final WebContext ctx,
                                            final OAuthRegisteredService svc) throws Exception {
    }
}
