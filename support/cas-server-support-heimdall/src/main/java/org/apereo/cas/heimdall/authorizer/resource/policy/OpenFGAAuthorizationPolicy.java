package org.apereo.cas.heimdall.authorizer.resource.policy;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.AuthorizationResult;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
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
import org.springframework.web.client.HttpClientErrorException;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link OpenFGAAuthorizationPolicy}.
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
@AllArgsConstructor
@Slf4j
public class OpenFGAAuthorizationPolicy implements ResourceAuthorizationPolicy {
    @Serial
    private static final long serialVersionUID = -1344481042826672523L;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @ExpressionLanguageCapable
    private String token;

    @ExpressionLanguageCapable
    private String apiUrl;

    @ExpressionLanguageCapable
    private String storeId;

    @ExpressionLanguageCapable
    private String relation;

    private String userType;
    
    @Override
    public AuthorizationResult evaluate(final AuthorizableResource resource, final AuthorizationRequest request) {
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
                .object(request.getNamespace() + ':' + request.getMethod() + ':' + request.getUri())
                .relation(StringUtils.defaultIfBlank(SpringExpressionLanguageValueResolver.getInstance().resolve(this.relation), "owner"))
                .user(StringUtils.defaultIfBlank(this.userType, "user") + ':' + request.getPrincipal().getId())
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
            val httpStatus = HttpStatus.resolve(response.getCode());
            LOGGER.debug("Received response status code [{}] from endpoint [{}]", httpStatus, url);
            if (httpStatus.is2xxSuccessful()) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val results = IOUtils.toString(content, StandardCharsets.UTF_8);
                    LOGGER.trace("Received response from endpoint [{}] as [{}]", url, results);
                    val payload = MAPPER.readValue(results, Map.class);
                    return AuthorizationResult.from((Boolean) payload.getOrDefault("allowed", Boolean.FALSE));
                }
            }
            throw new HttpClientErrorException(httpStatus);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return AuthorizationResult.denied("Denied");
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
