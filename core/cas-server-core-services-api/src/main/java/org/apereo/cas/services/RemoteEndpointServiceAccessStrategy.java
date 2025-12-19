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
        .defaultTypingEnabled(true).build().toObjectMapper();

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
                .entity(MAPPER.writeValueAsString(request))
                .build();
            val response = HttpUtils.execute(exec);
            val currentCodes = StringUtils.commaDelimitedListToSet(this.acceptableResponseCodes);
            return response != null && currentCodes.contains(String.valueOf(response.getCode()));
        }).get();
    }

}
