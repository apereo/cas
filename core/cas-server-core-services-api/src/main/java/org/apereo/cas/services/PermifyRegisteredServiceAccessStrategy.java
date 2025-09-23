package org.apereo.cas.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link PermifyRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Slf4j
public class PermifyRegisteredServiceAccessStrategy extends BaseRegisteredServiceAccessStrategy {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Serial
    private static final long serialVersionUID = -2108201604115278441L;
    
    private static final int DEPTH = 20;

    @ExpressionLanguageCapable
    private String apiUrl;

    @ExpressionLanguageCapable
    private String tenantId;

    @ExpressionLanguageCapable
    private String entityType;

    @ExpressionLanguageCapable
    private String subjectType;

    @ExpressionLanguageCapable
    private String subjectRelation;

    @ExpressionLanguageCapable
    private String permission;

    @ExpressionLanguageCapable
    private String token;

    @Override
    public boolean authorizeRequest(final RegisteredServiceAccessStrategyRequest request) throws Throwable {
        val expressionResolver = SpringExpressionLanguageValueResolver.getInstance();
        val permifyRequest = buildPermifyRequest(request);
        
        val url = Strings.CI.removeEnd(expressionResolver.resolve(this.apiUrl), "/");
        val tenant = Strings.CI.removeEnd(expressionResolver.resolve(this.tenantId), "/");
        val permifyUrl = String.format("%s/v1/tenants/%s/permissions/check", url, tenant);

        val headers = new HashMap<String, String>();
        headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + expressionResolver.resolve(this.token));
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        val exec = HttpExecutionRequest.builder()
            .method(HttpMethod.POST)
            .url(permifyUrl)
            .headers(headers)
            .entity(permifyRequest.toJson())
            .build();

        HttpResponse response = null;
        try {
            LOGGER.debug("Submitting authorization request to [{}] for [{}]", permifyUrl, permifyRequest);
            response = HttpUtils.execute(exec);
            try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                val results = IOUtils.toString(content, StandardCharsets.UTF_8);
                LOGGER.debug("Received response from endpoint [{}] as [{}]", url, results);
                val payload = MAPPER.readValue(results, Map.class);
                val result = (String) payload.get("can");
                return "CHECK_RESULT_ALLOWED".equalsIgnoreCase(result);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return false;
    }

    protected PermifyRequest buildPermifyRequest(final RegisteredServiceAccessStrategyRequest request) {
        val expressionResolver = SpringExpressionLanguageValueResolver.getInstance();
        val entity = new PermifyEntity(
            StringUtils.defaultIfBlank(expressionResolver.resolve(this.entityType), "application"),
            String.valueOf(request.getRegisteredService().getId()));

        val subject = new PermifySubject(
            StringUtils.defaultIfBlank(expressionResolver.resolve(subjectType), "user"),
            request.getPrincipalId(),
            StringUtils.defaultIfBlank(expressionResolver.resolve(subjectRelation), "owner"));

        val context = new PermifyContext(Map.of(
            "service", request.getService().getId(),
            "attributes", request.getAttributes())
        );
        
        return new PermifyRequest(entity, expressionResolver.resolve(permission),
            subject, context, new PermifyMetadata(DEPTH));
    }
    
    public record PermifyEntity(String type, String id) {
    }

    public record PermifySubject(String type, String id, String relation) {
    }

    public record PermifyContext(Map data) {
    }

    public record PermifyMetadata(int depth) {}

    public record PermifyRequest(PermifyEntity entity, String permission,
        PermifySubject subject, PermifyContext context, PermifyMetadata metadata) {

        /**
         * Convert to JSON.
         *
         * @return the string
         */
        public String toJson() {
            return FunctionUtils.doUnchecked(() -> MAPPER.writeValueAsString(this));
        }
    }
}
