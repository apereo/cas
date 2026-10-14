package org.apereo.cas.oidc.token;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.token.OAuth20TokenRequestValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;

/**
 * This is {@link OidcAccessTokenJwtBearerGrantRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public class OidcAccessTokenJwtBearerGrantRequestValidator implements OAuth20TokenRequestValidator {
    private int order = Ordered.LOWEST_PRECEDENCE;
    private final ObjectProvider<OidcConfigurationContext> configurationContext;

    @Override
    public boolean validate(final WebContext context) throws Throwable {
        val assertion = getConfigurationContext().getObject().getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.ASSERTION).orElse(StringUtils.EMPTY);
        val registeredService = resolveRegisteredService(assertion);
        if (registeredService.isPresent()) {
            getConfigurationContext().getObject().getProofOfPossessionValidator().validateTokenRequest(context, registeredService.get());
        }
        return true;
    }

    /**
     * The client of the grant is named by the assertion. The assertion itself is verified once the request is
     * extracted, and an assertion that names no known client is left for that step to refuse.
     *
     * @param assertion the assertion
     * @return the registered service, or empty when the assertion names no known client
     */
    protected Optional<OidcRegisteredService> resolveRegisteredService(final String assertion) {
        try {
            val clientId = OAuth20Utils.extractClientIdFromToken(assertion);
            if (StringUtils.isBlank(clientId)) {
                return Optional.empty();
            }
            return Optional.ofNullable(OAuth20Utils.getRegisteredOAuthServiceByClientId(
                getConfigurationContext().getObject().getServicesManager(), clientId, OidcRegisteredService.class));
        } catch (final Exception e) {
            LOGGER.debug("Unable to determine the client of the assertion: [{}]", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean supports(final WebContext webContext) throws Throwable {
        val grantType = getConfigurationContext().getObject().getRequestParameterResolver()
            .resolveRequestParameter(webContext, OAuth20Constants.GRANT_TYPE).orElse(StringUtils.EMPTY);
        val assertion = getConfigurationContext().getObject().getRequestParameterResolver()
            .resolveRequestParameter(webContext, OAuth20Constants.ASSERTION).orElse(StringUtils.EMPTY);
        return StringUtils.isNotBlank(assertion) && OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.JWT_BEARER);
    }
}
