package org.apereo.cas.oidc.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.util.OidcAuthorizationRequestSupport;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.views.OAuth20ConsentApprovalViewResolver;

import lombok.val;
import org.pac4j.core.context.JEEContext;

import java.util.HashSet;
import java.util.Map;

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
    protected boolean isConsentApprovalBypassed(final JEEContext context, final OAuthRegisteredService service) {
        if (service instanceof OidcRegisteredService) {
            val url = context.getFullRequestURL();
            val prompts = OidcAuthorizationRequestSupport.getOidcPromptFromAuthorizationRequest(url);
            if (prompts.contains(OidcConstants.PROMPT_CONSENT)) {
                return false;
            }
        }
        return super.isConsentApprovalBypassed(context, service);
    }

    @Override
    protected String getApprovalViewName() {
        return OidcConstants.CONFIRM_VIEW;
    }

    @Override
    protected void prepareApprovalViewModel(final Map<String, Object> model,
                                            final JEEContext ctx,
                                            final OAuthRegisteredService svc) throws Exception {
        super.prepareApprovalViewModel(model, ctx, svc);
        if (svc instanceof OidcRegisteredService) {
            val oidcRegisteredService = (OidcRegisteredService) svc;
            model.put("dynamic", oidcRegisteredService.isDynamicallyRegistered());
            model.put("dynamicTime", oidcRegisteredService.getDynamicRegistrationDateTime());
            val supportedScopes = new HashSet<String>(casProperties.getAuthn().getOidc().getScopes());
            supportedScopes.retainAll(oidcRegisteredService.getScopes());
            supportedScopes.retainAll(OAuth20Utils.getRequestedScopes(ctx));
            supportedScopes.add(OidcConstants.StandardScopes.OPENID.getScope());
            model.put("scopes", supportedScopes);
            val userInfoClaims = OAuth20Utils.parseUserInfoRequestClaims(ctx);
            model.put("userInfoClaims", userInfoClaims);
        }
    }
}
