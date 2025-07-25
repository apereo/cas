package org.apereo.cas.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link OpenFGARegisteredServiceAccessStrategy} that reaches out
 * to OpenFGA to check for user access.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Slf4j
public class OpenFGARegisteredServiceAccessStrategy extends BaseRegisteredServiceAccessStrategy {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Serial
    private static final long serialVersionUID = -1108201604115278440L;

    private String relation;

    private String object;

    @ExpressionLanguageCapable
    private String apiUrl;

    @ExpressionLanguageCapable
    private String storeId;

    @ExpressionLanguageCapable
    private String token;

    private String userType;

    @Override
    public boolean authorizeRequest(final RegisteredServiceAccessStrategyRequest request) {
        HttpResponse response = null;
        try {
            val headers = new HashMap<String, String>();
            headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            if (StringUtils.isNotBlank(token)) {
                headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + SpringExpressionLanguageValueResolver.getInstance().resolve(this.token));
            }
            val url = Strings.CI.removeEnd(SpringExpressionLanguageValueResolver.getInstance().resolve(this.apiUrl), "/");
            val store = Strings.CI.removeEnd(SpringExpressionLanguageValueResolver.getInstance().resolve(this.storeId), "/");
            val fgaApiUrl = String.format("%s/stores/%s/check", url, store);

            val checkEntity = AuthorizationRequestEntity.builder()
                .object(StringUtils.defaultIfBlank(this.object, request.getService().getId()))
                .relation(StringUtils.defaultIfBlank(this.relation, "owner"))
                .user(StringUtils.defaultIfBlank(this.userType, "user") + ':' + request.getPrincipalId())
                .build()
                .toJson();
            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.POST)
                .url(fgaApiUrl)
                .headers(headers)
                .entity(checkEntity)
                .build();
            LOGGER.debug("Submitting authorization request to [{}] for [{}]", fgaApiUrl, checkEntity);
            response = HttpUtils.execute(exec);
            if (HttpStatus.resolve(response.getCode()).is2xxSuccessful()) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val results = IOUtils.toString(content, StandardCharsets.UTF_8);
                    LOGGER.trace("Received response from endpoint [{}] as [{}]", url, results);
                    val payload = MAPPER.readValue(results, Map.class);
                    return (Boolean) payload.getOrDefault("allowed", Boolean.FALSE);
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return false;
    }

    @SuperBuilder
    @Getter
    private static final class AuthorizationRequestEntity {
        private final String user;

        private final String relation;

        private final String object;

        @JsonIgnore
        String toJson() {
            return FunctionUtils.doUnchecked(() -> MAPPER.writeValueAsString(Map.of("tuple_key", this)));
        }
    }
}
