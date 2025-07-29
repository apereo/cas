package org.apereo.cas.oidc.token.ciba;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcCibaRequest;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGeneratedResult;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.ModelAndView;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * This is {@link CibaPushTokenDeliveryHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class CibaPushTokenDeliveryHandler implements CibaTokenDeliveryHandler {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .singleValueAsArray(true).defaultTypingEnabled(false).build().toObjectMapper();

    private final OidcBackchannelTokenDeliveryModes deliveryMode = OidcBackchannelTokenDeliveryModes.PUSH;

    private final OidcConfigurationContext configurationContext;

    @Override
    public Map<String, ?> deliver(final OidcRegisteredService registeredService, final OidcCibaRequest cibaRequest) throws Throwable {
        val service = getConfigurationContext().getWebApplicationServiceServiceFactory().createService(cibaRequest.getClientId());
        val generatedTokenResult = generateAccessToken(registeredService, cibaRequest, service);
        val payload = generateResponsePayload(registeredService, cibaRequest, generatedTokenResult, service);
        sendPushNotification(registeredService, cibaRequest, payload.getModel());
        return payload.getModel();
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    protected void sendPushNotification(final OidcRegisteredService registeredService,
                                        final OidcCibaRequest cibaRequest,
                                        final Map payload) {

        val verification = configurationContext.getCasProperties().getAuthn().getOidc().getCiba().getVerification();
        val delayInSeconds = Beans.newDuration(verification.getDelay()).toSeconds();
        configurationContext.getTaskScheduler().schedule(
            () -> {
                HttpResponse response = null;
                try {
                    val clientNotificationValue = cibaRequest.getAuthentication().getSingleValuedAttribute(OidcConstants.CLIENT_NOTIFICATION_TOKEN, String.class);
                    val exec = HttpExecutionRequest.builder()
                        .bearerToken(clientNotificationValue)
                        .method(HttpMethod.POST)
                        .url(registeredService.getBackchannelClientNotificationEndpoint())
                        .entity(MAPPER.writeValueAsString(payload))
                        .httpClient(configurationContext.getHttpClient())
                        .headers(CollectionUtils.wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .build();
                    LOGGER.debug("Sending a POST request to [{}] with payload [{}]", exec.getUrl(), exec.getEntity());
                    response = HttpUtils.execute(exec);
                    if (response == null || !HttpStatus.valueOf(response.getCode()).is2xxSuccessful()) {
                        LOGGER.error("Unable to deliver tokens to client application [{}]", registeredService.getName());
                        configurationContext.getTicketRegistry().deleteTicket(cibaRequest);
                    }
                } catch (final Exception e) {
                    LoggingUtils.error(LOGGER, e);
                } finally {
                    HttpUtils.close(response);
                }
            },
            LocalDateTime.now(Clock.systemUTC()).plusSeconds(delayInSeconds).toInstant(ZoneOffset.UTC));
    }

    protected ModelAndView generateResponsePayload(final OidcRegisteredService registeredService,
                                                   final OidcCibaRequest cibaRequest,
                                                   final OAuth20TokenGeneratedResult generatedTokenResult,
                                                   final WebApplicationService service) {
        val tokenResult = OAuth20AccessTokenResponseResult
            .builder()
            .registeredService(registeredService)
            .accessTokenTimeout(generatedTokenResult.getAccessToken().orElseThrow().getExpirationPolicy().getTimeToLive())
            .responseType(OAuth20ResponseTypes.NONE)
            .casProperties(configurationContext.getCasProperties())
            .generatedToken(generatedTokenResult)
            .grantType(OAuth20GrantTypes.CIBA)
            .cibaRequestId(cibaRequest.getEncodedId())
            .service(service)
            .build();
        val payload = configurationContext.getAccessTokenResponseGenerator().generate(tokenResult);
        payload.addObject(OidcConstants.AUTH_REQ_ID, cibaRequest.getEncodedId());
        LOGGER.debug("Generated access token response payload [{}]", payload.getModel());
        return payload;
    }

    protected OAuth20TokenGeneratedResult generateAccessToken(final OidcRegisteredService registeredService,
                                                              final OidcCibaRequest cibaRequest,
                                                              final WebApplicationService service) throws Throwable {
        val tokenRequestContext = AccessTokenRequestContext
            .builder()
            .scopes(cibaRequest.getScopes())
            .grantType(OAuth20GrantTypes.CIBA)
            .registeredService(registeredService)
            .authentication(cibaRequest.getAuthentication())
            .service(service)
            .cibaRequestId(cibaRequest.getEncodedId())
            .generateRefreshToken(registeredService.isGenerateRefreshToken())
            .build();
        return configurationContext.getAccessTokenGenerator().generate(tokenRequestContext);
    }
}
