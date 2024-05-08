package org.apereo.cas.oidc.token.ciba;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcCibaRequest;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import java.util.Map;

/**
 * This is {@link CibaPushTokenDeliveryHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@Getter
public class CibaPushTokenDeliveryHandler implements CibaTokenDeliveryHandler {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .singleValueAsArray(true).defaultTypingEnabled(false).build().toObjectMapper();

    private final OidcBackchannelTokenDeliveryModes deliveryMode = OidcBackchannelTokenDeliveryModes.PUSH;

    private final OidcConfigurationContext configurationContext;

    @Override
    public Map<String, ?> deliver(final OidcRegisteredService registeredService, final OidcCibaRequest cibaRequest) throws Throwable {
        HttpResponse response = null;
        try {
            val clientNotificationValue = (String) cibaRequest.getAuthentication().getSingleValuedAttribute(OidcConstants.CLIENT_NOTIFICATION_TOKEN);
            val service = getConfigurationContext().getWebApplicationServiceServiceFactory().createService(cibaRequest.getClientId());
            val tokenRequestContext = AccessTokenRequestContext
                .builder()
                .scopes(cibaRequest.getScopes())
                .grantType(OAuth20GrantTypes.CIBA)
                .registeredService(registeredService)
                .authentication(cibaRequest.getAuthentication())
                .service(service)
                .build();
            val generatedTokenResult = configurationContext.getAccessTokenGenerator().generate(tokenRequestContext);
            val tokenResult = OAuth20AccessTokenResponseResult
                .builder()
                .registeredService(registeredService)
                .accessTokenTimeout(generatedTokenResult.getAccessToken().orElseThrow().getExpirationPolicy().getTimeToLive())
                .responseType(OAuth20ResponseTypes.NONE)
                .casProperties(configurationContext.getCasProperties())
                .generatedToken(generatedTokenResult)
                .grantType(OAuth20GrantTypes.CIBA)
                .service(service)
                .build();
            val payload = configurationContext.getAccessTokenResponseGenerator().generate(tokenResult);
            val exec = HttpExecutionRequest.builder()
                .bearerToken(clientNotificationValue)
                .method(HttpMethod.POST)
                .url(registeredService.getBackchannelClientNotificationEndpoint())
                .entity(MAPPER.writeValueAsString(payload.getModel()))
                .httpClient(configurationContext.getHttpClient())
                .build();
            response = HttpUtils.execute(exec);
            FunctionUtils.throwIf(!HttpStatus.valueOf(response.getCode()).is2xxSuccessful(),
                () -> new HttpException("Unable to deliver tokens to client application %s".formatted(registeredService.getName())));
            configurationContext.getTicketRegistry().deleteTicket(cibaRequest);
            return payload.getModel();
        } finally {
            HttpUtils.close(response);
        }
    }
}
