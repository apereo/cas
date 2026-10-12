package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link RemoteEndpointServiceAccessStrategy} that reaches out
 * to a remote endpoint, passing the CAS principal id to determine if access is allowed.
 * If the status code returned in the final response is not accepted by the policy here,
 * access shall be denied.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RemoteEndpointServiceAccessStrategy extends BaseRegisteredServiceAccessStrategy {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Serial
    private static final long serialVersionUID = -1108201604115278440L;

    private String endpointUrl;

    private String acceptableResponseCodes;

    private String method = "GET";

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Map<String, String> headers = new TreeMap<>();

    @Override
    public boolean authorizeRequest(final RegisteredServiceAccessStrategyRequest request) {
        return Unchecked.supplier(() -> {
            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.valueOf(this.method))
                .url(SpringExpressionLanguageValueResolver.getInstance().resolve(endpointUrl))
                .headers(headers)
                .parameters(CollectionUtils.wrap("username", request.getPrincipalId()))
                .entity(MAPPER.writeValueAsString(buildRequestPayload(request)))
                .build();
            val response = HttpUtils.execute(exec);
            try {
                val currentCodes = StringUtils.commaDelimitedListToSet(this.acceptableResponseCodes);
                return response != null && currentCodes.contains(String.valueOf(response.getCode()));
            } finally {
                HttpUtils.close(response);
            }
        }).get();
    }

    /**
     * Build the payload sent to the remote endpoint. Only identifying details of the service and
     * the principal are sent; the registered service definition itself is never sent, since it may
     * carry client secrets, keys and other credentials.
     *
     * @param request the request
     * @return the payload
     */
    static Map<String, Object> buildRequestPayload(final RegisteredServiceAccessStrategyRequest request) {
        val payload = new LinkedHashMap<String, Object>();
        payload.put("principalId", request.getPrincipalId());
        payload.put("attributes", request.getAttributes());
        Optional.ofNullable(request.getService())
            .ifPresent(service -> payload.put("service", Collections.singletonMap("id", service.getId())));
        Optional.ofNullable(request.getRegisteredService()).ifPresent(registeredService -> {
            val details = new LinkedHashMap<String, Object>();
            details.put("id", registeredService.getId());
            details.put("name", registeredService.getName());
            details.put("serviceId", registeredService.getServiceId());
            details.put("friendlyName", registeredService.getFriendlyName());
            payload.put("registeredService", details);
        });
        return payload;
    }
}
