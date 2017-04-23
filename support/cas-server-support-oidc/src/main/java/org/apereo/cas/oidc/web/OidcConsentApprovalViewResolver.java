package org.apereo.cas.oidc.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.util.OidcAuthorizationRequestSupport;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.views.OAuth20ConsentApprovalViewResolver;
import org.pac4j.core.context.J2EContext;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link OidcConsentApprovalViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcConsentApprovalViewResolver extends OAuth20ConsentApprovalViewResolver {

    public OidcConsentApprovalViewResolver(final CasConfigurationProperties casProperties) {
        super(casProperties);
    }

    @Override
    protected boolean isConsentApprovalBypassed(final J2EContext context, final OAuthRegisteredService service) {
        final String url = context.getFullRequestURL();
        final Set<String> prompts = OidcAuthorizationRequestSupport.getOidcPromptFromAuthorizationRequest(url);
        if (prompts.contains(OidcConstants.PROMPT_CONSENT) || service.isGenerateRefreshToken()) {
            return false;
        }
        return super.isConsentApprovalBypassed(context, service);
    }

    @Override
    protected String getApprovalViewName() {
        return OidcConstants.CONFIRM_VIEW;
    }

    @Override
    protected void prepareApprovalViewModel(final Map<String, Object> model, final J2EContext ctx, final OAuthRegisteredService svc) {
        super.prepareApprovalViewModel(model, ctx, svc);
        if (svc instanceof OidcRegisteredService) {
            final OidcRegisteredService oidcRegisteredService = (OidcRegisteredService) svc;
            model.put("dynamic", oidcRegisteredService.isDynamicallyRegistered());
            model.put("dynamicTime", oidcRegisteredService.getDynamicRegistrationDateTime());

            final Set<String> supportedScopes = new HashSet<>(casProperties.getAuthn().getOidc().getScopes());
            supportedScopes.retainAll(oidcRegisteredService.getScopes());
            supportedScopes.retainAll(OAuth20Utils.getRequestedScopes(ctx));
            model.put("scopes", supportedScopes);
        }
    }
}
