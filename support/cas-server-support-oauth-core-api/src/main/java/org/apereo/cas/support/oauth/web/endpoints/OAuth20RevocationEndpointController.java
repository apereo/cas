package org.apereo.cas.support.oauth.web.endpoints;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.JacksonJsonView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OAuth20RevocationEndpointController}.
 *
 * @author Julien Huon
 * @since 6.2.0
 */
@Slf4j
@Tag(name = "OAuth")
public class OAuth20RevocationEndpointController<T extends OAuth20ConfigurationContext> extends BaseOAuth20Controller<T> {
    public OAuth20RevocationEndpointController(final T oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    /**
     * Handle request for revocation.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     * @throws Throwable the throwable
     */
    @PostMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.REVOCATION_URL,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle OAuth token revocation request")
    public ModelAndView handleRequest(final HttpServletRequest request,
                                      final HttpServletResponse response)
            throws Throwable {

        if (!verifyRevocationRequest(request, response)) {
            LOGGER.error("Revocation request verification failed. Request is missing required parameters");
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
        }

        val clientId = getConfigurationContext().getRequestParameterResolver()
            .resolveClientIdAndClientSecret(request).getLeft();
        val registeredService = getRegisteredServiceByClientId(clientId);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
        
        if (OAuth20Utils.doesServiceNeedAuthentication(Objects.requireNonNull(registeredService))) {
            if (OAuth20Utils.findAuthenticatedUserProfile(request, response, getConfigurationContext().getSessionStore()).isEmpty()) {
                LOGGER.warn("Service [{}] requests authentication", clientId);
                return OAuth20Utils.writeError(response, OAuth20Constants.ACCESS_DENIED);
            }
        } else {
            val service = getConfigurationContext().getWebApplicationServiceServiceFactory()
                .createService(registeredService.getClientId());
            val audit = AuditableContext.builder()
                .service(service)
                .registeredService(registeredService)
                .build();
            val accessResult = getConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
            if (accessResult.isExecutionFailure()) {
                return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
            }
        }
        val token = Optional.ofNullable(request.getParameter(OAuth20Constants.TOKEN))
            .map(String::valueOf).orElse(StringUtils.EMPTY);

        return generateRevocationResponse(token, clientId, response);
    }

    protected ModelAndView generateRevocationResponse(final String token,
                                                      final String clientId,
                                                      final HttpServletResponse response)
            throws Exception {
        val registryToken = FunctionUtils.doAndHandle(() -> {
            val state = getConfigurationContext().getTicketRegistry().getTicket(extractAccessTokenFrom(token), OAuth20Token.class);
            return state == null || state.isExpired() ? null : state;
        });
        if (registryToken == null) {
            LOGGER.debug("Provided token [{}] has not been found in the ticket registry", token);
            return buildSuccessfulRevocationResponse();
        }
        
        if (isRefreshToken(registryToken) || isAccessToken(registryToken)) {
            if (!Objects.equals(clientId, registryToken.getClientId())) {
                LOGGER.warn("Provided token [{}] has not been issued for the service [{}]", token, clientId);
                return buildSuccessfulRevocationResponse();
            }
            if (isRefreshToken(registryToken)) {
                revokeToken((OAuth20RefreshToken) registryToken);
            } else {
                revokeToken(registryToken.getId());
            }
            return buildSuccessfulRevocationResponse();
        }
        LOGGER.error("Provided token [{}] is either not a refresh token or an access token", token);
        return buildSuccessfulRevocationResponse();
    }

    private static ModelAndView buildSuccessfulRevocationResponse() {
        val modelAndView = new ModelAndView(new JacksonJsonView());
        modelAndView.setStatus(HttpStatus.OK);
        return modelAndView;
    }

    private boolean verifyRevocationRequest(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        val validator = getConfigurationContext().getAccessTokenGrantRequestValidators().getObject()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .filter(Unchecked.predicate(requestValidator -> requestValidator.supports(request, response)))
            .findFirst()
            .orElse(null);
        if (validator == null) {
            LOGGER.warn("Ignoring malformed request [{}] as no OAuth20 validator could declare support for its syntax", HttpRequestUtils.getFullRequestUrl(request));
            return false;
        }
        return validator.validate(request, response);
    }
}
