package org.apereo.cas.oidc.vc.token;

import module java.base;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialTransactionService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link OidcVerifiableCredentialsPreAuthorizationCodeAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcVerifiableCredentialsPreAuthorizationCodeAuthenticator implements Authenticator {
    private final ObjectProvider<OidcVerifiableCredentialTransactionService> transactionService;
    private final PrincipalResolver principalResolver;
    private final OAuth20RequestParameterResolver requestParameterResolver;

    @Override
    public Optional<Credentials> validate(final CallContext ctx, final Credentials credentials) {
        return FunctionUtils.doUnchecked(() -> {
            val up = (UsernamePasswordCredentials) credentials;
            val grantType = up.getPassword();
            if (OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.PRE_AUTHORIZED_CODE)) {
                val providedPreAuthzCode = up.getUsername();
                val providedTxCode = requestParameterResolver.resolveRequestParameter(ctx.webContext(), OidcConstants.TX_CODE)
                    .orElse(StringUtils.EMPTY);

                val preAuthorizationCode = (TransientSessionTicket) transactionService.getObject().fetchPreAuthorizationCode(providedPreAuthzCode);
                val principalId = Objects.requireNonNull(preAuthorizationCode).getPropertyAsString("principalId");
                val credentialConfigurationIds = Objects.requireNonNull(preAuthorizationCode).getProperty("credentialConfigurationIds", List.class);
                val clientId = Objects.requireNonNull(preAuthorizationCode).getPropertyAsString(OAuth20Constants.CLIENT_ID);

                val transactionCode = preAuthorizationCode.getPropertyAsString("transactionId");
                val validTransaction = StringUtils.isBlank(providedTxCode) || Strings.CI.equals(transactionCode, providedTxCode);

                if (validTransaction) {
                    val principal = principalResolver.resolve(new BasicIdentifiableCredential(principalId));
                    val profile = new CommonProfile();
                    profile.setId(principal.getId());
                    profile.addAttribute(OAuth20Constants.CLIENT_ID, clientId);
                    profile.addAttribute("credentialConfigurationIds", credentialConfigurationIds);
                    profile.addAttributes((Map) principal.getAttributes());

                    LOGGER.debug("Authenticated user profile [{}]", profile);
                    credentials.setUserProfile(profile);
                    return Optional.of(credentials);
                }
            }
            return Optional.empty();
        });
    }
}
