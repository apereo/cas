package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.util.OidcAuthorizationRequestSupport;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.views.OAuth20ConsentApprovalViewResolver;
import org.pac4j.core.context.J2EContext;

import java.util.Set;

/**
 * This is {@link OidcConsentApprovalViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcConsentApprovalViewResolver extends OAuth20ConsentApprovalViewResolver {

    @Override
    protected boolean isConsentApprovalBypassed(final J2EContext context, final OAuthRegisteredService service) {
        final String url = context.getFullRequestURL();
        final Set<String> prompts = OidcAuthorizationRequestSupport.getOidcPromptFromAuthorizationRequest(url);
        if (prompts.contains(OidcConstants.PROMPT_CONSENT)) {
            return false;
        }
        return super.isConsentApprovalBypassed(context, service);
    }
}
