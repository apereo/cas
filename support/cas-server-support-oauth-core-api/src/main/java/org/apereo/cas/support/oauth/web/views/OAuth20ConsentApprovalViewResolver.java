package org.apereo.cas.support.oauth.web.views;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.web.servlet.ModelAndView;

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

    protected final OAuth20RequestParameterResolver oauthRequestParameterResolver;

    @Override
    public ModelAndView resolve(final WebContext context, final OAuthRegisteredService service) throws Exception {
        // do not store approval, if it is always bypassed
        if (isConsentApprovalBypassed(context, service)) {
            return new ModelAndView();
        }

        var bypassApprovalParameter = context.getRequestParameter(OAuth20Constants.BYPASS_APPROVAL_PROMPT)
            .map(String::valueOf).orElse(StringUtils.EMPTY).equalsIgnoreCase(Boolean.TRUE.toString());
        val approvalKey = OAuth20Constants.BYPASS_APPROVAL_PROMPT + "_" + service.getClientId();
        var approvedScopes = new HashSet<>(sessionStore
                .get(context, approvalKey)
                .map(o -> o instanceof Collection ? ((Collection<?>) o).stream()
                                                    .map(String::valueOf)
                                                    .collect(Collectors.toSet()) : null)
                .orElse(Set.of()));
        val requestedScopes = resolveRequestedScopes(context, service);
        if (!bypassApprovalParameter && approvedScopes.containsAll(requestedScopes)) {
            bypassApprovalParameter = true;
        }
        LOGGER.trace("Bypassing approval prompt for service [{}]: [{}]", service, bypassApprovalParameter);
        if (bypassApprovalParameter) {
            approvedScopes.addAll(requestedScopes);
            sessionStore.set(context, approvalKey, approvedScopes);
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
     * Get a collection of requested scopes
     * @param context the context
     * @return a collection of requested scopes
     */
    protected Collection<String> resolveRequestedScopes(final WebContext context, final OAuthRegisteredService service) {
        val supportedScopes = new HashSet<>(casProperties.getAuthn().getOidc().getDiscovery().getScopes());
        supportedScopes.retainAll(service.getScopes());
        supportedScopes.retainAll(oauthRequestParameterResolver.resolveRequestedScopes(context));
        return supportedScopes;
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
