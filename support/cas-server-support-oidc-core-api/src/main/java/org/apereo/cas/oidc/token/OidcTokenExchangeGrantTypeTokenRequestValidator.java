package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.token.OAuth20TokenExchangeGrantTypeTokenRequestValidator;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link OidcTokenExchangeGrantTypeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class OidcTokenExchangeGrantTypeTokenRequestValidator extends OAuth20TokenExchangeGrantTypeTokenRequestValidator<OidcConfigurationContext> {
    public OidcTokenExchangeGrantTypeTokenRequestValidator(
        final ObjectProvider<OidcConfigurationContext> configurationContext) {
        super(configurationContext);
    }

    @Override
    protected OAuthRegisteredService extractRegisteredService(final String subjectTokenType,
                                                              final String subjectToken) throws Exception {
        val configurationContext = getConfigurationContext().getObject();

        if (configurationContext.getDiscoverySettings().isNativeSsoSupported()
            && OAuth20TokenExchangeTypes.from(subjectTokenType) == OAuth20TokenExchangeTypes.ID_TOKEN) {
            val clientIdInIdToken = OAuth20Utils.extractClientIdFromToken(subjectToken);
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), clientIdInIdToken);
            val claims = configurationContext.getIdTokenSigningAndEncryptionService().decode(subjectToken, Optional.ofNullable(registeredService));
            val service = configurationContext.getWebApplicationServiceServiceFactory().createService(claims.getIssuer());
            service.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of(clientIdInIdToken));
            return OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), clientIdInIdToken);
        }
        return super.extractRegisteredService(subjectTokenType, subjectToken);
    }
}
