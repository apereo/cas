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
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.AnonymousProfile;

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
    public AccessTokenRequestContext extractRequest(final WebContext context) throws Throwable {
        val clientId = getConfigurationContext().getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.CLIENT_ID).orElse(StringUtils.EMPTY);
        LOGGER.debug("Locating OAuth registered service by client id [{}]", clientId);

        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(getConfigurationContext().getServicesManager(), clientId);
        LOGGER.debug("Located OAuth registered service [{}]", registeredService);

        val deviceCode = getConfigurationContext().getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.DEVICE_CODE).orElse(StringUtils.EMPTY);
        val service = getConfigurationContext().getAuthenticationBuilder().buildService(registeredService, context, false);

        LOGGER.debug("Authenticating the OAuth request indicated by [{}]", service);
        val authentication = getConfigurationContext().getAuthenticationBuilder().build(new AnonymousProfile(),
            registeredService, context, service);

        val audit = AuditableContext.builder()
            .service(service)
            .registeredService(registeredService)
            .authentication(authentication)
            .build();
        val accessResult = getConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        accessResult.throwExceptionIfNeeded();

        return AccessTokenRequestContext.builder()
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
    public boolean supports(final WebContext context) {
        val responseType = getConfigurationContext().getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.RESPONSE_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val grantType = getConfigurationContext().getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.GRANT_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val clientId = getConfigurationContext().getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.CLIENT_ID)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val validRequest = OAuth20Utils.isResponseType(responseType, OAuth20ResponseTypes.DEVICE_CODE)
                           || OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.DEVICE_CODE);
        return validRequest && StringUtils.isNotBlank(clientId);

    }

    @Override
    public OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.DEVICE_CODE;
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
