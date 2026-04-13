package org.apereo.cas.oidc.vc.token;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialTransactionService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.BaseAccessTokenGrantRequestExtractor;
import org.apereo.cas.ticket.TransientSessionTicket;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link AccessTokenPreAuthorizedCodeGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
public class AccessTokenPreAuthorizedCodeGrantRequestExtractor<T extends OAuth20ConfigurationContext> extends BaseAccessTokenGrantRequestExtractor<T> {
    private final OidcVerifiableCredentialTransactionService transactionService;

    public AccessTokenPreAuthorizedCodeGrantRequestExtractor(
        final ObjectProvider<T> configurationContext,
        final OidcVerifiableCredentialTransactionService transactionService) {
        super(configurationContext);
        this.transactionService = transactionService;
    }

    @Override
    public boolean supports(final WebContext context) {
        val grantType = getConfigurationContext().getObject().getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.GRANT_TYPE).orElse(StringUtils.EMPTY);
        return OAuth20Utils.isGrantType(grantType, getGrantType());
    }

    @Override
    public OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.PRE_AUTHORIZED_CODE;
    }

    @Override
    public OAuth20ResponseTypes getResponseType() {
        return OAuth20ResponseTypes.NONE;
    }

    @Override
    protected AccessTokenRequestContext extractRequest(final WebContext webContext) throws Throwable {
        val configurationContext = getConfigurationContext().getObject();
        val requestParameterResolver = configurationContext.getRequestParameterResolver();

        val scopes = requestParameterResolver.resolveRequestScopes(webContext);

        val txCode = requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.TX_CODE).orElseThrow();
        val transaction = Objects.requireNonNull((TransientSessionTicket) transactionService.fetch(txCode));
        val clientId = transaction.getPropertyAsString(OAuth20Constants.CLIENT_ID);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            configurationContext.getServicesManager(), Objects.requireNonNull(clientId));

        val service = configurationContext.getWebApplicationServiceServiceFactory().createService(clientId);
        service.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of(clientId));

        val userProfile = extractUserProfile(webContext).orElseThrow();

        val audit = AuditableContext.builder()
            .service(service)
            .registeredService(registeredService)
            .build();
        val accessResult = configurationContext.getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        accessResult.throwExceptionIfNeeded();
        
        val principal = configurationContext.getPrincipalResolver().resolve(
            new BasicIdentifiableCredential(Objects.requireNonNull(transaction.getPropertyAsString("principalId"))));
        principal.getAttributes().forEach(userProfile::addAttribute);
        val authentication = configurationContext.getAuthenticationBuilder()
            .build(userProfile, Objects.requireNonNull(registeredService), webContext, service);

        return AccessTokenRequestContext
            .builder()
            .scopes(scopes)
            .grantType(getGrantType())
            .registeredService(registeredService)
            .service(service)
            .authentication(authentication)
            .build();
    }
}
