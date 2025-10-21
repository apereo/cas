package org.apereo.cas.heimdall.authorizer.resource.policy;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.AuthorizationResult;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;
import java.io.Serial;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link RestfulAuthorizationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@NoArgsConstructor
@Slf4j
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RestfulAuthorizationPolicy implements ResourceAuthorizationPolicy {
    @Serial
    private static final long serialVersionUID = -1244481042826672523L;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).minimal(true).build().toObjectMapper();
    
    /**
     * The endpoint URL to contact and retrieve attributes.
     */
    @ExpressionLanguageCapable
    private String url;

    /**
     * Headers, defined as a Map, to include in the request when making the REST call.
     */
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Map<String, String> headers = new HashMap<>();

    @JsonSetter(nulls = Nulls.SKIP)
    private String method = "POST";

    @Override
    public AuthorizationResult evaluate(final AuthorizableResource resource, final AuthorizationRequest request) throws Throwable {
        val entity = Map.of("resource", resource, "request", request);
        val finalHeaders = headers
            .entrySet()
            .stream()
            .map(entry -> {
                val key = entry.getKey();
                val value = SpringExpressionLanguageValueResolver.getInstance().resolve(entry.getValue());
                return Map.entry(key, value);
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        val exec = HttpExecutionRequest.builder()
            .method(HttpMethod.valueOf(method.toUpperCase(Locale.ENGLISH).trim()))
            .url(url)
            .headers(finalHeaders)
            .entity(MAPPER.writeValueAsString(entity))
            .build();
        val response = HttpUtils.execute(exec);
        val authorized = response != null && HttpStatus.valueOf(response.getCode()).is2xxSuccessful();
        return authorized ? AuthorizationResult.granted("OK") : AuthorizationResult.denied("Denied");
    }
}
