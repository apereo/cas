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
import lombok.AllArgsConstructor;
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
 * This is {@link CerbosRegisteredServiceAccessStrategy} that reaches out
 * to Cerbos to check for resource access.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Slf4j
public class CerbosRegisteredServiceAccessStrategy extends BaseRegisteredServiceAccessStrategy {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Serial
    private static final long serialVersionUID = -2218211604445278440L;

    @ExpressionLanguageCapable
    private String apiUrl;

    @ExpressionLanguageCapable
    private String token;

    @ExpressionLanguageCapable
    private String scope;

    @ExpressionLanguageCapable
    private String rolesAttribute = "memberOf";

    @ExpressionLanguageCapable
    private String kind;

    @ExpressionLanguageCapable
    private String requestId;

    private List<String> actions;

    private Map<String, Object> auxData;

    @Override
    public boolean authorizeRequest(final RegisteredServiceAccessStrategyRequest request) {
        HttpResponse response = null;
        try {
            val attributes = new HashMap<>(request.getAttributes());
            attributes.put("serviceUrl", List.of(request.getService().getId()));
            attributes.put("serviceName", List.of(request.getRegisteredService().getName()));
            attributes.put("serviceId", List.of(request.getRegisteredService().getId()));
            attributes.put("serviceFriendlyName", List.of(request.getRegisteredService().getFriendlyName()));
            attributes.put("serviceType", List.of(request.getRegisteredService().getClass().getSimpleName()));

            val roles = attributes.getOrDefault(this.rolesAttribute, List.of()).stream().map(Object::toString).toList();
            val cerbosRequest = CerbosRequest
                .builder()
                .requestId(SpringExpressionLanguageValueResolver.getInstance().resolve(this.requestId))
                .principal(CerbosPrincipal.builder()
                    .id(request.getPrincipalId())
                    .scope(this.scope)
                    .roles(roles)
                    .attr(attributes)
                    .build())
                .resources(List.of(CerbosResources.builder()
                    .resource(CerbosResource.builder()
                        .id(String.valueOf(request.getRegisteredService().getId()))
                        .kind(this.kind)
                        .scope(this.scope)
                        .attr(attributes)
                        .build())
                    .actions(this.actions.toArray(ArrayUtils.EMPTY_STRING_ARRAY))
                    .build()))
                .auxData(this.auxData)
                .build();
            val headers = new HashMap<String, String>();
            headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            if (StringUtils.isNotBlank(token)) {
                headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + SpringExpressionLanguageValueResolver.getInstance().resolve(this.token));
            }

            val givenUrl = StringUtils.defaultIfBlank(SpringExpressionLanguageValueResolver.getInstance()
                .resolve(this.apiUrl), "http://localhost:3592");
            val url = StringUtils.removeEnd(givenUrl, "/") + "/api/check/resources";
            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.POST)
                .url(url)
                .headers(headers)
                .entity(cerbosRequest.toJson())
                .build();
            LOGGER.debug("Submitting authorization request to [{}] for [{}]", url, cerbosRequest);
            response = HttpUtils.execute(exec);

            try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                val results = IOUtils.toString(content, StandardCharsets.UTF_8);
                LOGGER.trace("Received response from endpoint [{}] as [{}]", url, results);
                val payload = MAPPER.readValue(results, CerboseResponse.class);
                if (HttpStatus.resolve(response.getCode()).is2xxSuccessful()
                    && StringUtils.equals(cerbosRequest.getRequestId(), payload.getRequestId())) {
                    return payload.getResults().isEmpty() || payload.getResults().stream().allMatch(result -> actions.stream().allMatch(action -> {
                        val actionResult = result.getActions().get(action);
                        return actionResult != Actions.EFFECT_DENY;
                    }));
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
    private static final class CerbosRequest {
        private final String requestId;

        private final CerbosPrincipal principal;
        private final List<CerbosResources> resources;
        private final Map<String, Object> auxData;

        @Builder.Default
        private final boolean includeMeta = true;

        @JsonIgnore
        public String toJson() {
            return FunctionUtils.doUnchecked(() -> MAPPER.writeValueAsString(this));
        }
    }

    @SuperBuilder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    private static final class CerbosPrincipal {
        private String id;
        private String policyVersion;
        private String scope;
        private List<String> roles;
        private Map<String, ?> attr;
    }

    @SuperBuilder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    private static final class CerbosResources {
        private CerbosResource resource;
        private String[] actions;
    }

    @SuperBuilder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    private static final class CerbosResource {
        private String kind;
        private String id;
        private String policyVersion;
        private String scope;
        private Map<String, ?> attr;
    }

    @SuperBuilder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    private static final class CerboseResponse {
        private String requestId;
        private List<CerbosResult> results;
        private String cerbosCallId;
    }

    @SuperBuilder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    private static final class CerbosResult {
        private CerbosResource resource;
        private Map<String, Actions> actions;
        private Map<String, Actions> validationErrors;
        private Map<String, Object> meta;
    }

    /**
     * Enum representing the possible authorization effect types returned by Cerbos API.
     */
    private enum Actions {
        /**
         * Indicates that the effect is not specified or unknown.
         */
        EFFECT_UNSPECIFIED,

        /**
         * Indicates that the action is allowed.
         */
        EFFECT_ALLOW,

        /**
         * Indicates that the action is denied.
         */
        EFFECT_DENY,

        /**
         * Indicates that no policy matched the request.
         */
        EFFECT_NO_MATCH
    }
}
