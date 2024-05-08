package org.apereo.cas.oidc.web.response;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.response.introspection.OAuth20DefaultIntrospectionResponseGenerator;
import org.apereo.cas.support.oauth.web.response.introspection.OAuth20IntrospectionAccessTokenResponse;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import java.util.Optional;

/**
 * This is {@link OidcIntrospectionResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Getter
public class OidcIntrospectionResponseGenerator extends OAuth20DefaultIntrospectionResponseGenerator {
    protected final ObjectProvider<OidcConfigurationContext> oidcConfigurationContext;

    @Override
    protected OAuth20IntrospectionAccessTokenResponse collectIntrospectionDetails(final OAuth20IntrospectionAccessTokenResponse response,
                                                                                  final OAuth20Token accessToken) {
        super.collectIntrospectionDetails(response, accessToken);

        if (accessToken instanceof final ServiceAwareTicket sat) {
            val service = sat.getService();
            val registeredService = oidcConfigurationContext.getObject().getServicesManager().findServiceBy(service, OidcRegisteredService.class);
            response.setIss(oidcConfigurationContext.getObject().getIssuerService().determineIssuer(Optional.ofNullable(registeredService)));
        }
        FunctionUtils.doIf(response.isActive(), __ -> response.setScope(String.join(" ", accessToken.getScopes()))).accept(response);
        CollectionUtils.firstElement(accessToken.getAuthentication().getAttributes().get(OAuth20Constants.DPOP_CONFIRMATION))
            .ifPresent(dpop -> response.getConfirmation().setJkt(dpop.toString()));
        return response;
    }

    @Override
    public boolean supports(final OAuth20Token accessToken) {
        return super.supports(accessToken)
            && accessToken != null
            && accessToken.getScopes().contains(OidcConstants.StandardScopes.OPENID.getScope());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
