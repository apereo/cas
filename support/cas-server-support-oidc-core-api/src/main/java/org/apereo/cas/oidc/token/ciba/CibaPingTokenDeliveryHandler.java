package org.apereo.cas.oidc.token.ciba;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcCibaRequest;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link CibaPingTokenDeliveryHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class CibaPingTokenDeliveryHandler implements CibaTokenDeliveryHandler {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .singleValueAsArray(true).defaultTypingEnabled(false).build().toObjectMapper();

    private final OidcBackchannelTokenDeliveryModes deliveryMode = OidcBackchannelTokenDeliveryModes.PING;

    private final OidcConfigurationContext configurationContext;

    @Override
    public Map<String, ?> deliver(final OidcRegisteredService registeredService, final OidcCibaRequest cibaRequest) throws Throwable {
        HttpResponse response = null;
        try {
            val clientNotificationValue = cibaRequest.getAuthentication().getSingleValuedAttribute(OidcConstants.CLIENT_NOTIFICATION_TOKEN, String.class);
            val payload = Map.of(OidcConstants.AUTH_REQ_ID, cibaRequest.getEncodedId());
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
            FunctionUtils.throwIf(!HttpStatus.valueOf(response.getCode()).is2xxSuccessful(),
                () -> new HttpException("Unable to deliver tokens to client application %s".formatted(registeredService.getName())));
            LOGGER.debug("Marking CIBA authentication request [{}] as ready", cibaRequest.getEncodedId());
            configurationContext.getTicketRegistry().updateTicket(cibaRequest.markTicketReady());
            return payload;
        } finally {
            HttpUtils.close(response);
        }
    }
}
