package org.apereo.cas.oidc.vc.token;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialTransactionService;
import org.apereo.cas.oidc.vc.services.OidcVerifiableCredentialPolicyUtils;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20AccessTokenGeneratorCustomizer;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link OidcVerifiableCredentialsAccessTokenGeneratorCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class OidcVerifiableCredentialsAccessTokenGeneratorCustomizer implements OAuth20AccessTokenGeneratorCustomizer {
    private final CasConfigurationProperties casProperties;

    @Override
    public void customize(final AccessTokenRequestContext tokenRequestContext, final OAuth20AccessToken accessToken) {
        if (tokenRequestContext.getGrantType() == OAuth20GrantTypes.PRE_AUTHORIZED_CODE) {
            val configurationIds = tokenRequestContext.getParameters()
                .get(OidcVerifiableCredentialTransactionService.PROPERTY_CREDENTIAL_CONFIGURATION_IDS);
            if (configurationIds instanceof final List<?> values && !values.isEmpty()) {
                accessToken.setCredentialConfigurationIds(values.stream().map(Object::toString).toList());
            }
        } else if (tokenRequestContext.getGrantType() == OAuth20GrantTypes.AUTHORIZATION_CODE
            || tokenRequestContext.getGrantType() == OAuth20GrantTypes.REFRESH_TOKEN) {
            val configurationIds = resolveScopedCredentialConfigurationIds(tokenRequestContext);
            if (!configurationIds.isEmpty()) {
                accessToken.setCredentialConfigurationIds(configurationIds);
            }
        }
    }

    /**
     * Credential configurations requested by scope. OpenID4VCI 1.0 section 5.1.2 has the issuer interpret every
     * granted scope that is the {@code scope} of a credential configuration as a request for that configuration,
     * as an alternative to authorization details. The result is narrowed by the service's verifiable credentials
     * policy, so a scope never authorizes a credential type the service may not obtain.
     *
     * @param tokenRequestContext the token request context
     * @return the credential configuration ids
     */
    protected List<String> resolveScopedCredentialConfigurationIds(final AccessTokenRequestContext tokenRequestContext) {
        val configurations = casProperties.getAuthn().getOidc().getVc().getIssuer().getCredentialConfigurations();
        val allowed = OidcVerifiableCredentialPolicyUtils.resolveAllowedCredentialConfigurationIds(
            tokenRequestContext.getRegisteredService(), configurations.keySet());
        val grantedScopes = tokenRequestContext.getScopes();
        return configurations
            .entrySet()
            .stream()
            .filter(entry -> allowed.contains(entry.getKey()))
            .filter(entry -> StringUtils.isNotBlank(entry.getValue().getScope()) && grantedScopes.contains(entry.getValue().getScope()))
            .map(Map.Entry::getKey)
            .toList();
    }
}
