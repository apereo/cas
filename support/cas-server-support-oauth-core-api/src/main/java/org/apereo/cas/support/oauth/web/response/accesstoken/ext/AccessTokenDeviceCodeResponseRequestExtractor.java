package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link AccessTokenDeviceCodeResponseRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class AccessTokenDeviceCodeResponseRequestExtractor extends BaseAccessTokenGrantRequestExtractor<OAuth20ConfigurationContext> {
    private static final String ANONYMOUS_PROFILE_ID = "anonymous";

    public AccessTokenDeviceCodeResponseRequestExtractor(final ObjectProvider<OAuth20ConfigurationContext> oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    @Override
    public AccessTokenRequestContext extractRequest(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        val configurationContext = getConfigurationContext().getObject();
        val clientId = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(request, OAuth20Constants.CLIENT_ID).orElse(StringUtils.EMPTY);
        LOGGER.debug("Locating OAuth registered service by client id [{}]", clientId);

        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), clientId);
        LOGGER.debug("Located OAuth registered service [{}]", registeredService);

        val deviceCode = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(request, OAuth20Constants.DEVICE_CODE).orElse(StringUtils.EMPTY);
        val service = configurationContext.getAuthenticationBuilder().buildService(registeredService, request, false);

        LOGGER.debug("Authenticating the OAuth request indicated by [{}]", service);
        val authentication = configurationContext.getAuthenticationBuilder()
            .build(new DefaultOAuth20UserProfile(ANONYMOUS_PROFILE_ID, OAuth20UserProfile.ANONYMOUS_PROFILE_HANDLER_NAME),
                registeredService, request, service);

        val audit = AuditableContext.builder()
            .service(service)
            .registeredService(registeredService)
            .authentication(authentication)
            .build();
        val accessResult = configurationContext.getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        accessResult.throwExceptionIfNeeded();

        val scopes = configurationContext.getRequestParameterResolver().resolveRequestScopes(request);
        scopes.retainAll(Objects.requireNonNull(registeredService).getScopes());
        
        return AccessTokenRequestContext.builder()
            .service(service)
            .authentication(authentication)
            .scopes(scopes)
            .registeredService(registeredService)
            .responseType(getResponseType())
            .grantType(getGrantType())
            .generateRefreshToken(registeredService != null && registeredService.isGenerateRefreshToken())
            .deviceCode(deviceCode)
            .resources(configurationContext.getRequestParameterResolver().resolveRequestedResources(request))
            .build();
    }

    @Override
    public boolean supports(final HttpServletRequest request) {
        val configurationContext = getConfigurationContext().getObject();
        val responseType = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(request, OAuth20Constants.RESPONSE_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val grantType = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(request, OAuth20Constants.GRANT_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val clientId = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(request, OAuth20Constants.CLIENT_ID)
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
}
