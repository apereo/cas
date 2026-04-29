package org.apereo.cas.support.oauth.web.views;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
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

    protected final CasConfigurationProperties casProperties;

    protected final SessionStore sessionStore;

    protected final OAuth20RequestParameterResolver oauthRequestParameterResolver;

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

    protected boolean isConsentApprovalBypassed(final WebContext context, final OAuthRegisteredService service) {
        return service.isBypassApprovalPrompt()
               || casProperties.getAuthn().getOauth().getCore().isBypassApprovalPrompt();
    }

    protected ModelAndView redirectToApproveView(final WebContext context,
                                                 final OAuthRegisteredService service) throws Exception {
        val callbackUrl = context.getFullRequestURL();
        LOGGER.trace("Requesting URL to call back: [{}]", callbackUrl);

        val url = new URIBuilder(callbackUrl);
        url.addParameter(OAuth20Constants.BYPASS_APPROVAL_PROMPT, Boolean.TRUE.toString());
        val model = new HashMap<String, Object>();
        model.put("service", service);
        model.put("callbackUrl", url.toString());
        FunctionUtils.doIfNotNull(service.getAccessStrategy().getUnauthorizedRedirectUrl(),
            u -> model.put("deniedApprovalUrl", u));

        val supportedScopes = new HashSet<>(service.getScopes());
        val requestedScopes = oauthRequestParameterResolver.resolveRequestedScopes(context);
        supportedScopes.retainAll(requestedScopes);
        model.put("scopes", supportedScopes);
        
        prepareApprovalViewModel(model, context, service);
        return getApprovalModelAndView(model);
    }

    protected ModelAndView getApprovalModelAndView(final Map<String, Object> model) {
        return new ModelAndView(getApprovalViewName(), model);
    }

    protected String getApprovalViewName() {
        return OAuth20Constants.CONFIRM_VIEW;
    }

    protected void prepareApprovalViewModel(final Map<String, Object> model,
                                            final WebContext context,
                                            final OAuthRegisteredService service) throws Exception {
    }
}
