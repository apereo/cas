package org.apereo.cas.oidc.vc.token;

import module java.base;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialTransactionService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientAuthenticationToken;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientCredentialsAuthenticator;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link OidcVerifiableCredentialsPreAuthorizationCodeAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcVerifiableCredentialsPreAuthorizationCodeAuthenticator implements OAuth20ClientCredentialsAuthenticator {
    private final ObjectProvider<OidcVerifiableCredentialTransactionService> transactionService;
    private final PrincipalResolver principalResolver;
    private final OAuth20RequestParameterResolver requestParameterResolver;

    @Override
    public Optional<OAuth20UserProfile> authenticate(final OAuth20ClientAuthenticationToken token) {
        return FunctionUtils.doUnchecked(() -> {
            val grantType = Objects.requireNonNull((String) token.getCredentials());
            if (OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.PRE_AUTHORIZED_CODE)) {
                val providedPreAuthzCode = Objects.requireNonNull(token.getUsername());
                val preAuthorizationCode = (TransientSessionTicket) transactionService.getObject().fetchPreAuthorizationCode(providedPreAuthzCode);
                if (preAuthorizationCode == null || preAuthorizationCode.isExpired()) {
                    LOGGER.warn("Pre-authorization code [{}] is not found or has expired", providedPreAuthzCode);
                    return Optional.<OAuth20UserProfile>empty();
                }

                val providedTxCode = requestParameterResolver.resolveRequestParameter(token.getRequest(), OidcConstants.TX_CODE).orElse(null);
                val principalId = preAuthorizationCode.getPropertyAsString("principalId");
                val credentialConfigurationIds = OidcVerifiableCredentialTransactionService.getCredentialConfigurationIds(preAuthorizationCode);
                val clientId = preAuthorizationCode.getPropertyAsString(OAuth20Constants.CLIENT_ID);

                if (transactionService.getObject().isTransactionCodeValid(preAuthorizationCode, providedTxCode)) {
                    val principal = principalResolver.resolve(new BasicIdentifiableCredential(principalId));
                    val profile = new DefaultOAuth20UserProfile(principal.getId(), OAuth20UserProfile.COMMON_PROFILE_HANDLER_NAME);
                    profile.addAttribute(OAuth20Constants.CLIENT_ID, clientId);
                    profile.addAttribute("credentialConfigurationIds", credentialConfigurationIds);
                    profile.addAttributes(principal.getAttributes());

                    LOGGER.debug("Authenticated user profile [{}]", profile);
                    return Optional.<OAuth20UserProfile>of(profile);
                }
            }
            return Optional.<OAuth20UserProfile>empty();
        });
    }
}
