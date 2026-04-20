package org.apereo.cas.oidc.vc.token;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialTransactionService;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.validator.token.BaseOAuth20TokenRequestValidator;
import org.apereo.cas.ticket.TransientSessionTicket;
import lombok.Getter;
import lombok.val;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;

/**
 * This is {@link OidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Getter
public class OidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator extends BaseOAuth20TokenRequestValidator<OidcConfigurationContext> {
    private final int order = Ordered.LOWEST_PRECEDENCE;

    private final OidcVerifiableCredentialTransactionService transactionService;

    public OidcVerifiableCredentialsPreAuthorizationCodeGrantRequestValidator(
        final ObjectProvider<OidcConfigurationContext> configurationContext,
        final OidcVerifiableCredentialTransactionService transactionService) {
        super(configurationContext);
        this.transactionService = transactionService;
    }

    @Override
    protected boolean validateInternal(final WebContext context, final String grantType, final ProfileManager manager,
                                       final UserProfile userProfile) throws Throwable {
        val requestParameterResolver = getConfigurationContext().getObject().getRequestParameterResolver();
        val txCode = requestParameterResolver.resolveRequestParameter(context, OidcConstants.TX_CODE).orElseThrow();
        val preAuthCode = requestParameterResolver.resolveRequestParameter(context, OidcConstants.PRE_AUTHORIZED_CODE).orElseThrow();
        val transaction = (TransientSessionTicket) transactionService.fetch(txCode);
        return preAuthCode.equalsIgnoreCase(Objects.requireNonNull(transaction).getPropertyAsString("preAuthorizedCode"));
    }

    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.PRE_AUTHORIZED_CODE;
    }
}
