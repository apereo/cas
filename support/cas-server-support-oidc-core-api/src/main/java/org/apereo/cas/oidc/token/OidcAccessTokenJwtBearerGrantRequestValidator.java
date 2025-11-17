package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.OidcConfigurationContext;
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
import org.jspecify.annotations.NonNull;
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
    private final ObjectProvider<@NonNull OidcConfigurationContext> configurationContext;

    @Override
    public boolean validate(final WebContext context) throws Throwable {
        return true;
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
