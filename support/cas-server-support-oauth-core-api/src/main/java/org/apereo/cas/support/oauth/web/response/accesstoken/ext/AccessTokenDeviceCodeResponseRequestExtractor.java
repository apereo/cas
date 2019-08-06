package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.AnonymousProfile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link AccessTokenDeviceCodeResponseRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class AccessTokenDeviceCodeResponseRequestExtractor extends BaseAccessTokenGrantRequestExtractor {
    public AccessTokenDeviceCodeResponseRequestExtractor(final OAuth20ConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    @Override
    public AccessTokenRequestDataHolder extract(final HttpServletRequest request, final HttpServletResponse response) {
        val clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        LOGGER.debug("Locating OAuth registered service by client id [{}]", clientId);

        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(getOAuthConfigurationContext().getServicesManager(), clientId);
        LOGGER.debug("Located OAuth registered service [{}]", registeredService);

        val deviceCode = request.getParameter(OAuth20Constants.CODE);

        val context = new JEEContext(request, response, getOAuthConfigurationContext().getSessionStore());
        val service = getOAuthConfigurationContext().getAuthenticationBuilder().buildService(registeredService, context, false);

        LOGGER.debug("Authenticating the OAuth request indicated by [{}]", service);
        val authentication = getOAuthConfigurationContext().getAuthenticationBuilder().build(new AnonymousProfile(), registeredService, context, service);

        val audit = AuditableContext.builder()
            .service(service)
            .registeredService(registeredService)
            .authentication(authentication)
            .build();
        val accessResult = getOAuthConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        accessResult.throwExceptionIfNeeded();

        return AccessTokenRequestDataHolder.builder()
            .service(service)
            .authentication(authentication)
            .registeredService(registeredService)
            .responseType(getResponseType())
            .grantType(getGrantType())
            .generateRefreshToken(registeredService != null && registeredService.isGenerateRefreshToken())
            .deviceCode(deviceCode)
            .build();
    }

    @Override
    public boolean supports(final HttpServletRequest context) {
        val responseType = context.getParameter(OAuth20Constants.RESPONSE_TYPE);
        val clientId = context.getParameter(OAuth20Constants.CLIENT_ID);
        return OAuth20Utils.isResponseType(responseType, OAuth20ResponseTypes.DEVICE_CODE)
            && StringUtils.isNotBlank(clientId);
    }

    @Override
    public OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.NONE;
    }

    @Override
    public OAuth20ResponseTypes getResponseType() {
        return OAuth20ResponseTypes.DEVICE_CODE;
    }

    @Override
    public boolean requestMustBeAuthenticated() {
        return false;
    }
}
