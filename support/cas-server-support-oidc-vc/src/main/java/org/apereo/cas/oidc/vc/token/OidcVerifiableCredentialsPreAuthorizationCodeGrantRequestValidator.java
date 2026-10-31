package org.apereo.cas.oidc.vc.token;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialTransactionService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.token.BaseOAuth20TokenRequestValidator;
import org.apereo.cas.ticket.TransientSessionTicket;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    protected boolean validateInternal(final HttpServletRequest request, final HttpServletResponse response, final String grantType,
                                       final OAuth20UserProfile userProfile)
            throws Throwable {
        val requestParameterResolver = getConfigurationContext().getObject().getRequestParameterResolver();
        val preAuthCode = requestParameterResolver.resolveRequestParameter(request, OidcConstants.PRE_AUTHORIZED_CODE);
        if (preAuthCode.isEmpty()) {
            return false;
        }
        val ticket = (TransientSessionTicket) transactionService.fetchPreAuthorizationCode(preAuthCode.get());
        if (ticket == null || ticket.isExpired()) {
            return false;
        }
        val transactionCode = requestParameterResolver.resolveRequestParameter(request, OidcConstants.TX_CODE).orElse(null);
        if (!transactionService.isTransactionCodeValid(ticket, transactionCode)) {
            return false;
        }
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(getConfigurationContext().getObject().getServicesManager(),
            StringUtils.defaultString(ticket.getPropertyAsString(OAuth20Constants.CLIENT_ID)));
        if (registeredService != null) {
            OAuth20Utils.verifyRequestedResources(requestParameterResolver, request, registeredService);
        }
        return true;
    }

    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.PRE_AUTHORIZED_CODE;
    }
}
