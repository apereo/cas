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
import lombok.Builder;
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
import java.util.List;
import java.util.Map;

/**
 * This is {@link OpenPolicyAgentRegisteredServiceAccessStrategy} that reaches out
 * to OPA to check for user access.
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
public class OpenPolicyAgentRegisteredServiceAccessStrategy extends BaseRegisteredServiceAccessStrategy {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Serial
    private static final long serialVersionUID = -2218201604115278440L;

    private String decision;

    @ExpressionLanguageCapable
    private String apiUrl;

    @ExpressionLanguageCapable
    private String token;

    private Map<String, Object> context = new HashMap<>();

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
            val rule = Strings.CI.removeEnd(SpringExpressionLanguageValueResolver.getInstance().resolve(this.decision), "/");
            val opaUrl = String.format("%s/v1/data/%s", url, rule);

            val checkEntity = AuthorizationRequestEntity.builder()
                .attributes(request.getAttributes())
                .service(request.getService().getId())
                .principal(request.getPrincipalId())
                .context(this.context)
                .build()
                .toJson();
            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.POST)
                .url(opaUrl)
                .headers(headers)
                .entity(checkEntity)
                .build();
            LOGGER.debug("Submitting authorization request to [{}] for [{}]", opaUrl, checkEntity);
            response = HttpUtils.execute(exec);
            if (HttpStatus.resolve(response.getCode()).is2xxSuccessful()) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val results = IOUtils.toString(content, StandardCharsets.UTF_8);
                    LOGGER.trace("Received response from endpoint [{}] as [{}]", url, results);
                    val payload = MAPPER.readValue(results, Map.class);
                    return (Boolean) payload.getOrDefault("result", Boolean.FALSE);
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
        private final String principal;

        private final String service;

        @Builder.Default
        private final Map<String, List<Object>> attributes = new HashMap<>();

        @Builder.Default
        private final Map<String, Object> context = new HashMap<>();

        @JsonIgnore
        String toJson() {
            return FunctionUtils.doUnchecked(() -> MAPPER.writeValueAsString(Map.of("input", this)));
        }
    }
}
