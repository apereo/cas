package org.apereo.cas.support.oauth.validator.token;

import module java.base;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20ProofOfPossessionValidator;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OAuth20DeviceCodeResponseTypeRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Slf4j
@Getter
@Setter
public class OAuth20DeviceCodeResponseTypeRequestValidator implements OAuth20TokenRequestValidator {
    private final ServicesManager servicesManager;

    private final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;

    private final OAuth20RequestParameterResolver requestParameterResolver;

    private final OAuth20ProofOfPossessionValidator proofOfPossessionValidator;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public boolean validate(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        val responseType = requestParameterResolver.resolveRequestParameter(request, OAuth20Constants.RESPONSE_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val grantType = requestParameterResolver.resolveRequestParameter(request, OAuth20Constants.GRANT_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val validResponseType = OAuth20Utils.isResponseType(responseType, OAuth20ResponseTypes.DEVICE_CODE);
        val validGrantType = OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.DEVICE_CODE);

        if (!validResponseType && !validGrantType) {
            LOGGER.warn("Response type [{}] or grant type [{}] is not supported.", responseType, grantType);
            return false;
        }

        val clientId = requestParameterResolver.resolveRequestParameter(request, OAuth20Constants.CLIENT_ID).orElse(StringUtils.EMPTY);
        val authorized = FunctionUtils.doAndHandle(() -> {
            val registeredService = Objects.requireNonNull(OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId));
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
            return (validResponseType && requestParameterResolver.isAuthorizedResponseTypeForService(request, registeredService))
                   || (validGrantType && requestParameterResolver.isAuthorizedGrantTypeForService(request, registeredService));
        }, t -> {
            LOGGER.warn("Registered service access is not allowed for service definition for client id [{}]", clientId);
            return false;
        }).get();
        if (authorized) {
            validateProofOfPossession(request, response, clientId);
            validateRequestedResources(request, clientId, validResponseType);
        }
        return authorized;
    }

    /**
     * The device authorization request may name the resources the tokens are for (RFC 8707), which must be ones the
     * service allows. The device code then carries them over to the token request that redeems it.
     *
     * @param request                    the request
     * @param clientId                   the client id
     * @param deviceAuthorizationRequest whether this is the device authorization request rather than the token request
     */
    protected void validateRequestedResources(final HttpServletRequest request, final String clientId,
                                              final boolean deviceAuthorizationRequest) {
        if (deviceAuthorizationRequest) {
            val registeredService = Objects.requireNonNull(OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId));
            OAuth20Utils.verifyRequestedResources(requestParameterResolver, request, registeredService);
        }
    }

    /**
     * Polling for the token with the device code is a token request, so its DPoP proof is verified and handed over for
     * the token to be bound to the proof's key (RFC 9449, section 5). The device authorization request that hands out
     * the device code is not, and is left alone.
     *
     * @param request  the request
     * @param response the response
     * @param clientId the client id
     * @throws Throwable when the proof does not verify, or is missing for a client that must present one
     */
    protected void validateProofOfPossession(final HttpServletRequest request, final HttpServletResponse response,
                                             final String clientId)
            throws Throwable {
        val deviceCode = requestParameterResolver.resolveRequestParameter(request, OAuth20Constants.DEVICE_CODE);
        if (deviceCode.filter(StringUtils::isNotBlank).isPresent()) {
            val registeredService = Objects.requireNonNull(OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId));
            proofOfPossessionValidator.validateTokenRequest(request, response, registeredService);
        }
    }

    @Override
    public boolean supports(final HttpServletRequest request, final HttpServletResponse response) {
        val responseType = requestParameterResolver.resolveRequestParameter(request, OAuth20Constants.RESPONSE_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val grantType = requestParameterResolver.resolveRequestParameter(request, OAuth20Constants.GRANT_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val clientId = requestParameterResolver.resolveRequestParameter(request, OAuth20Constants.CLIENT_ID)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val validRequest = OAuth20Utils.isResponseType(responseType, OAuth20ResponseTypes.DEVICE_CODE)
                           || OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.DEVICE_CODE);
        return validRequest && StringUtils.isNotBlank(clientId);
    }
}
