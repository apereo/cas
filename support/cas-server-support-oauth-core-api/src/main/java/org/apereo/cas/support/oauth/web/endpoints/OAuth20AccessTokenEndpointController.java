package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.events.OAuth20AccessTokenRequestEvent;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.token.device.InvalidOAuth20DeviceTokenException;
import org.apereo.cas.support.oauth.validator.token.device.ThrottledOAuth20DeviceUserCodeApprovalException;
import org.apereo.cas.support.oauth.validator.token.device.UnapprovedOAuth20DeviceUserCodeException;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGeneratedResult;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.OAuth20UnauthorizedScopeRequestException;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import com.google.common.base.Supplier;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.context.WebContext;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This controller returns an access token according to the given
 * OAuth code and client credentials (authorization code grant type)
 * or according to the refresh token and client credentials
 * (refresh token grant type) or according to the user identity
 * (resource owner password grant type).
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Slf4j
public class OAuth20AccessTokenEndpointController<T extends OAuth20ConfigurationContext> extends BaseOAuth20Controller<T> {
    private static final Map<String, AccessTokenExceptionResponses> ACCESS_TOKEN_RESPONSE_EXCEPTIONS = Map.of(
        InvalidOAuth20DeviceTokenException.class.getName(),
        new AccessTokenExceptionResponses(OAuth20Constants.ACCESS_DENIED,
            "Could not identify and extract device token request for device token"),

        UnapprovedOAuth20DeviceUserCodeException.class.getName(),
        new AccessTokenExceptionResponses(OAuth20Constants.AUTHORIZATION_PENDING,
            "User code is not yet approved for the device token request"),

        ThrottledOAuth20DeviceUserCodeApprovalException.class.getName(),
        new AccessTokenExceptionResponses(OAuth20Constants.SLOW_DOWN,
            "Device user code approval is too quick and is throttled. Requests must slow down"),

        OAuth20UnauthorizedScopeRequestException.class.getName(),
        new AccessTokenExceptionResponses(OAuth20Constants.INVALID_SCOPE,
            "Invalid or unauthorized scope")
    );

    private final AuditableExecution accessTokenGrantAuditableRequestExtractor;

    public OAuth20AccessTokenEndpointController(final T oauthConfigurationContext,
                                                final AuditableExecution accessTokenGrantAuditableRequestExtractor) {
        super(oauthConfigurationContext);
        this.accessTokenGrantAuditableRequestExtractor = accessTokenGrantAuditableRequestExtractor;
    }

    private static ModelAndView handleAccessTokenException(final Throwable exception, final HttpServletResponse response) {
        val data = ACCESS_TOKEN_RESPONSE_EXCEPTIONS.getOrDefault(exception.getClass().getName(),
            new AccessTokenExceptionResponses(OAuth20Constants.INVALID_GRANT, "Invalid or unauthorized grant"));
        LoggingUtils.error(LOGGER, String.format("%s: %s", data.message(), exception.getMessage()), exception);
        return OAuth20Utils.writeError(response, data.code(), data.message());
    }

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @PostMapping(path = {
        OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.ACCESS_TOKEN_URL,
        OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.TOKEN_URL},
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        val context = new JEEContext(request, response);
        try {
            if (!verifyAccessTokenRequest(context)) {
                LoggingUtils.error(LOGGER, "Access token validation failed");
                return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_GRANT);
            }
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
        }

        try {
            val tokenRequestContext = examineAndExtractAccessTokenGrantRequest(request, response);
            logProtocolRequest(tokenRequestContext);
            LOGGER.debug("Creating access token for [{}]", tokenRequestContext);
            val generatedTokenResult = getConfigurationContext().getAccessTokenGenerator().generate(tokenRequestContext);
            LOGGER.debug("Access token generated result is: [{}]", generatedTokenResult);
            return generateAccessTokenResponse(tokenRequestContext, generatedTokenResult);
        } catch (final Throwable e) {
            return handleAccessTokenException(e, response);
        }
    }

    private void logProtocolRequest(final AccessTokenRequestContext tokenRequestContext) {
        var authn = tokenRequestContext.getAuthentication();
        if (authn == null && tokenRequestContext.getTicketGrantingTicket() instanceof final AuthenticationAwareTicket aat) {
            authn = aat.getAuthentication();
        }
        Objects.requireNonNull(authn, "No authentication is available to handle this request");
        val protocolContext = Map.of(
            "Token", Optional.ofNullable(tokenRequestContext.getToken()).map(OAuth20Token::getId).orElse("none"),
            "Device Code", StringUtils.defaultString(tokenRequestContext.getDeviceCode()),
            "Scopes", String.join(",", tokenRequestContext.getScopes()),
            "Registered Service", tokenRequestContext.getRegisteredService().getName(),
            "Service", tokenRequestContext.getService().getId(),
            "Principal", authn.getPrincipal().getId(),
            "Grant Type", tokenRequestContext.getGrantType().getType(),
            "Response Type", tokenRequestContext.getResponseType().getType());
        LoggingUtils.protocolMessage("OAuth/OpenID Connect Token Request", protocolContext);
        configurationContext.getApplicationContext().publishEvent(
            new OAuth20AccessTokenRequestEvent(this, ClientInfoHolder.getClientInfo(), protocolContext));
    }

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping(path = {OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.ACCESS_TOKEN_URL,
        OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.TOKEN_URL})
    public ModelAndView handleGetRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return handleRequest(request, response);
    }

    protected ModelAndView generateAccessTokenResponse(
        final AccessTokenRequestContext tokenRequestContext,
        final OAuth20TokenGeneratedResult result) {
        return new OAuth20AccessTokenResponseEncoder(getConfigurationContext()).encode(tokenRequestContext, result);
    }

    @SuppressWarnings("UnusedVariable")
    private record AccessTokenExceptionResponses(String code, String message) {
    }

    private AccessTokenRequestContext examineAndExtractAccessTokenGrantRequest(final HttpServletRequest request,
                                                                               final HttpServletResponse response) throws Throwable {
        val audit = AuditableContext
            .builder()
            .httpRequest(request)
            .httpResponse(response)
            .build();
        val accessResult = accessTokenGrantAuditableRequestExtractor.execute(audit);
        val execResult = accessResult.getExecutionResult();
        return (AccessTokenRequestContext) execResult.orElseThrow(
            () -> new UnsupportedOperationException("Access token request is not supported"));
    }

    private boolean verifyAccessTokenRequest(final WebContext context) throws Throwable {
        val validators = getConfigurationContext().getAccessTokenGrantRequestValidators().getObject();
        return validators
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .filter(Unchecked.predicate(ext -> ext.supports(context)))
            .findFirst()
            .orElseThrow((Supplier<RuntimeException>) () -> new UnsupportedOperationException("Access token request is not supported"))
            .validate(context);
    }
}
