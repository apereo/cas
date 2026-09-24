package org.apereo.cas.oidc.vc.authz;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.oidc.vc.services.OidcVerifiableCredentialPolicyUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationCodeAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.util.CollectionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.ModelAndView;

/**
 * This is {@link OidcVerifiableCredentialAuthorizationCodeAuthorizationResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
@Slf4j
public class OidcVerifiableCredentialAuthorizationCodeAuthorizationResponseBuilder
    extends OAuth20AuthorizationCodeAuthorizationResponseBuilder {

    private int order = Ordered.HIGHEST_PRECEDENCE;

    public OidcVerifiableCredentialAuthorizationCodeAuthorizationResponseBuilder(
        final OAuth20ConfigurationContext context,
        final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder) {
        super(context, authorizationModelAndViewBuilder);
    }

    @Audit(action = AuditableActions.OAUTH2_AUTHORIZATION_RESPONSE,
        actionResolverName = AuditActionResolvers.OAUTH2_AUTHORIZATION_RESPONSE_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.OAUTH2_AUTHORIZATION_RESPONSE_RESOURCE_RESOLVER)
    @Override
    public ModelAndView build(final AccessTokenRequestContext tokenRequestContext) throws Throwable {
        val authzDetails = extractAuthorizationDetails(tokenRequestContext.getRegisteredService(),
            tokenRequestContext.getAuthorizationDetails());
        if (authzDetails.isEmpty()) {
            LOGGER.warn("Client [{}] requested authorization details [{}] that cannot be granted",
                tokenRequestContext.getClientId(), tokenRequestContext.getAuthorizationDetails());
            val params = new LinkedHashMap<String, String>();
            params.put(OAuth20Constants.ERROR, OAuth20Constants.INVALID_AUTHORIZATION_DETAILS);
            CollectionUtils.firstElement(tokenRequestContext.getAuthentication().getAttributes().get(OAuth20Constants.STATE))
                .ifPresent(state -> params.put(OAuth20Constants.STATE, state.toString()));
            return build(tokenRequestContext.getRegisteredService(), tokenRequestContext.getResponseMode(),
                tokenRequestContext.getRedirectUri(), params);
        }
        return super.build(tokenRequestContext);
    }

    @Override
    protected OAuth20Code createOAuthCode(final AccessTokenRequestContext tokenRequestContext) throws Throwable {
        val authzDetails = extractAuthorizationDetails(tokenRequestContext.getRegisteredService(),
            tokenRequestContext.getAuthorizationDetails());
        val code = super.createOAuthCode(tokenRequestContext);
        code.setAuthorizationDetails(authzDetails);
        code.setIssuerState(StringUtils.stripToNull(tokenRequestContext.getIssuerState()));
        return code;
    }

    @Override
    public boolean supports(final OAuth20AuthorizationRequest context) {
        return super.supports(context)
            && StringUtils.isNotBlank(context.getAuthorizationDetails())
            && !OidcVerifiableCredentialAuthorizationDetails.parse(context.getAuthorizationDetails()).isEmpty();
    }

    /**
     * Authorization details that survive as a grant: the intersection of what the wallet asked for,
     * what the issuer publishes, and what this relying party's verifiable credentials policy permits.
     *
     * @param registeredService    the registered service
     * @param authorizationDetails the requested authorization details
     * @return the authorized details, which may be empty
     */
    protected List<OidcVerifiableCredentialAuthorizationDetails> extractAuthorizationDetails(
        final RegisteredService registeredService, final String authorizationDetails) {
        val properties = configurationContext.getCasProperties().getAuthn().getOidc().getVc();
        val allowed = OidcVerifiableCredentialPolicyUtils.resolveAllowedCredentialConfigurationIds(
            registeredService, properties.getIssuer().getCredentialConfigurations().keySet());
        return OidcVerifiableCredentialAuthorizationDetails.from(authorizationDetails, allowed);
    }
}
