package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.token.OAuth20TokenRequestValidator;
import org.apereo.cas.support.oauth.validator.token.device.InvalidOAuth20DeviceTokenException;
import org.apereo.cas.support.oauth.validator.token.device.ThrottledOAuth20DeviceUserCodeApprovalException;
import org.apereo.cas.support.oauth.validator.token.device.UnapprovedOAuth20DeviceUserCodeException;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGeneratedResult;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;

import com.google.common.base.Supplier;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.J2EContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

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
    private final OAuth20TokenGenerator accessTokenGenerator;
    private final OAuth20AccessTokenResponseGenerator accessTokenResponseGenerator;
    private final ExpirationPolicy accessTokenExpirationPolicy;
    private final Collection<OAuth20TokenRequestValidator> accessTokenGrantRequestValidators;
    private final ExpirationPolicy deviceTokenExpirationPolicy;
    private final AuditableExecution accessTokenGrantAuditableRequestExtractor;

    public OAuth20AccessTokenEndpointController(final ServicesManager servicesManager,
                                                final TicketRegistry ticketRegistry,
                                                final AccessTokenFactory accessTokenFactory,
                                                final PrincipalFactory principalFactory,
                                                final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                                final OAuth20TokenGenerator accessTokenGenerator,
                                                final OAuth20AccessTokenResponseGenerator accessTokenResponseGenerator,
                                                final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                                final CasConfigurationProperties casProperties,
                                                final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                                final ExpirationPolicy accessTokenExpirationPolicy,
                                                final ExpirationPolicy deviceTokenExpirationPolicy,
                                                final Collection<OAuth20TokenRequestValidator> accessTokenGrantRequestValidators,
                                                final AuditableExecution accessTokenGrantAuditableRequestExtractor) {
        super(servicesManager,
            ticketRegistry,
            accessTokenFactory,
            principalFactory,
            webApplicationServiceServiceFactory,
            scopeToAttributesFilter,
            casProperties,
            ticketGrantingTicketCookieGenerator);

        this.accessTokenGenerator = accessTokenGenerator;
        this.accessTokenResponseGenerator = accessTokenResponseGenerator;
        this.accessTokenExpirationPolicy = accessTokenExpirationPolicy;
        this.deviceTokenExpirationPolicy = deviceTokenExpirationPolicy;
        this.accessTokenGrantRequestValidators = accessTokenGrantRequestValidators;
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
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
        }

        try {
            val requestHolder = examineAndExtractAccessTokenGrantRequest(request, response);
            LOGGER.debug("Creating access token for [{}]", requestHolder);
            val context = Pac4jUtils.getPac4jJ2EContext(request, response);
            val tokenResult = accessTokenGenerator.generate(requestHolder);
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
    protected ModelAndView generateAccessTokenResponse(final HttpServletRequest request, final HttpServletResponse response,
                                                       final AccessTokenRequestDataHolder requestHolder,
                                                       final J2EContext context, final OAuth20TokenGeneratedResult result) {
        LOGGER.debug("Generating access token response for [{}]", result);

        val deviceRefreshInterval = Beans.newDuration(casProperties.getAuthn().getOauth().getDeviceToken().getRefreshInterval()).getSeconds();
        val tokenResult = OAuth20AccessTokenResponseResult.builder()
            .registeredService(requestHolder.getRegisteredService())
            .service(requestHolder.getService())
            .accessTokenTimeout(accessTokenExpirationPolicy.getTimeToLive())
            .deviceRefreshInterval(deviceRefreshInterval)
            .deviceTokenTimeout(deviceTokenExpirationPolicy.getTimeToLive())
            .responseType(result.getResponseType().isPresent() ? result.getResponseType().get() : OAuth20ResponseTypes.NONE)
            .casProperties(casProperties)
            .generatedToken(result)
            .build();
        return this.accessTokenResponseGenerator.generate(request, response, tokenResult);
    }

    private AccessTokenRequestDataHolder examineAndExtractAccessTokenGrantRequest(final HttpServletRequest request,
                                                                                  final HttpServletResponse response) {
        val audit = AuditableContext.builder()
            .httpRequest(request)
            .httpResponse(response)
            .build();
        val accessResult = this.accessTokenGrantAuditableRequestExtractor.execute(audit);
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
        if (accessTokenGrantRequestValidators.isEmpty()) {
            LOGGER.warn("No validators are defined to examine the access token request for eligibility");
            return false;
        }
        val context = new J2EContext(request, response);
        return this.accessTokenGrantRequestValidators.stream()
            .filter(ext -> ext.supports(context))
            .findFirst()
            .orElseThrow((Supplier<RuntimeException>) () -> new UnsupportedOperationException("Access token request is not supported"))
            .validate(context);
    }
}
