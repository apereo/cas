package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.token.device.InvalidOAuth20DeviceTokenException;
import org.apereo.cas.support.oauth.validator.token.device.ThrottledOAuth20DeviceUserCodeApprovalException;
import org.apereo.cas.support.oauth.validator.token.device.UnapprovedOAuth20DeviceUserCodeException;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGeneratedResult;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import org.apereo.cas.ticket.OAuth20UnauthorizedScopeRequestException;

import com.google.common.base.Supplier;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class OAuth20AccessTokenEndpointController extends BaseOAuth20Controller {
    private final AuditableExecution accessTokenGrantAuditableRequestExtractor;

    public OAuth20AccessTokenEndpointController(final OAuth20ConfigurationContext oauthConfigurationContext,
                                                final AuditableExecution accessTokenGrantAuditableRequestExtractor) {
        super(oauthConfigurationContext);
        this.accessTokenGrantAuditableRequestExtractor = accessTokenGrantAuditableRequestExtractor;
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
    @SneakyThrows
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        try {
            if (!verifyAccessTokenRequest(request, response)) {
                throw new IllegalArgumentException("Access token validation failed");
            }
        } catch (final OAuth20UnauthorizedScopeRequestException e) {
            LOGGER.error(e.getMessage(), e);
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_SCOPE);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
        }

        try {
            val requestHolder = examineAndExtractAccessTokenGrantRequest(request, response);
            LOGGER.debug("Creating access token for [{}]", requestHolder);
            val context = new JEEContext(request, response, getOAuthConfigurationContext().getSessionStore());
            val tokenResult = getOAuthConfigurationContext().getAccessTokenGenerator().generate(requestHolder);
            LOGGER.debug("Access token generated result is: [{}]", tokenResult);
            return generateAccessTokenResponse(request, response, requestHolder, context, tokenResult);
        } catch (final InvalidOAuth20DeviceTokenException e) {
            LOGGER.error("Could not identify and extract device token request for device token [{}]", e.getTicketId());
            return OAuth20Utils.writeError(response, OAuth20Constants.ACCESS_DENIED);
        } catch (final UnapprovedOAuth20DeviceUserCodeException e) {
            LOGGER.error("User code [{}] is not yet approved for the device token request", e.getTicketId());
            return OAuth20Utils.writeError(response, OAuth20Constants.AUTHORIZATION_PENDING);
        } catch (final ThrottledOAuth20DeviceUserCodeApprovalException e) {
            LOGGER.error("Check for device user code approval is too quick and is throttled. Requests must slow down");
            return OAuth20Utils.writeError(response, OAuth20Constants.SLOW_DOWN);
        } catch (final OAuth20UnauthorizedScopeRequestException e) {
            LOGGER.error(e.getMessage(), e);
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_SCOPE);
        } catch (final Exception e) {
            LOGGER.error("Could not identify and extract access token request", e);
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_GRANT);
        }
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

    private AccessTokenRequestDataHolder examineAndExtractAccessTokenGrantRequest(final HttpServletRequest request,
                                                                                  final HttpServletResponse response) {
        val audit = AuditableContext.builder()
            .httpRequest(request)
            .httpResponse(response)
            .build();
        val accessResult = accessTokenGrantAuditableRequestExtractor.execute(audit);
        val execResult = accessResult.getExecutionResult();
        if (execResult.isPresent()) {
            return (AccessTokenRequestDataHolder) execResult.get();
        }
        throw new UnsupportedOperationException("Access token request is not supported");
    }

    /**
     * Verify the access token request.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return true, if successful
     */
    private boolean verifyAccessTokenRequest(final HttpServletRequest request, final HttpServletResponse response) {
        val validators = getOAuthConfigurationContext().getAccessTokenGrantRequestValidators();
        if (validators.isEmpty()) {
            LOGGER.warn("No validators are defined to examine the access token request for eligibility");
            return false;
        }
        val context = new JEEContext(request, response);
        return validators.stream()
            .filter(ext -> ext.supports(context))
            .findFirst()
            .orElseThrow((Supplier<RuntimeException>) () -> new UnsupportedOperationException("Access token request is not supported"))
            .validate(context);
    }

    /**
     * Generate access token response model and view.
     *
     * @param request       the request
     * @param response      the response
     * @param requestHolder the request holder
     * @param context       the context
     * @param result        the result
     * @return the model and view
     */
    protected ModelAndView generateAccessTokenResponse(final HttpServletRequest request,
                                                       final HttpServletResponse response,
                                                       final AccessTokenRequestDataHolder requestHolder,
                                                       final JEEContext context,
                                                       final OAuth20TokenGeneratedResult result) {
        LOGGER.debug("Generating access token response for [{}]", result);
        val deviceRefreshInterval = Beans.newDuration(getOAuthConfigurationContext().getCasProperties()
            .getAuthn().getOauth().getDeviceToken().getRefreshInterval()).getSeconds();
        val atPolicy = getOAuthConfigurationContext().getAccessTokenExpirationPolicy();
        val dtPolicy = getOAuthConfigurationContext().getDeviceTokenExpirationPolicy();
        val tokenResult = OAuth20AccessTokenResponseResult.builder()
            .registeredService(requestHolder.getRegisteredService())
            .service(requestHolder.getService())
            .accessTokenTimeout(atPolicy.buildTicketExpirationPolicy().getTimeToLive())
            .deviceRefreshInterval(deviceRefreshInterval)
            .deviceTokenTimeout(dtPolicy.buildTicketExpirationPolicy().getTimeToLive())
            .responseType(result.getResponseType().orElse(OAuth20ResponseTypes.NONE))
            .casProperties(getOAuthConfigurationContext().getCasProperties())
            .generatedToken(result)
            .build();
        return getOAuthConfigurationContext().getAccessTokenResponseGenerator().generate(request, response, tokenResult);
    }
}
