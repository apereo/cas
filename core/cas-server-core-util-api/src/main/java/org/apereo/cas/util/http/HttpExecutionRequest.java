package org.apereo.cas.util.http;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link HttpExecutionRequest}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SuperBuilder
@Getter
@With
@AllArgsConstructor
public class HttpExecutionRequest {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final HttpClient httpClient;

    @NonNull
    private final HttpMethod method;

    @NonNull
    private final String url;

    private final String basicAuthUsername;

    private final String basicAuthPassword;

    private final String entity;

    private final String proxyUrl;

    private final String bearerToken;

    @Builder.Default
    private final int maximumRetryAttempts = 3;

    @Builder.Default
    private final Map<String, String> parameters = new LinkedHashMap<>();

    @Builder.Default
    private final Map<String, String> headers = new LinkedHashMap<>();

    /**
     * Is basic authentication?
     *
     * @return true/false
     */
    boolean isBasicAuthentication() {
        return StringUtils.isNotBlank(basicAuthUsername) && StringUtils.isNotBlank(basicAuthPassword);
    }

    /**
     * Is bearer authentication?
     *
     * @return true/false
     */
    boolean isBearerAuthentication() {
        return StringUtils.isNotBlank(bearerToken);
    }


    /**
     * Convert this record into JSON.
     *
     * @return the string
     */
    @JsonIgnore
    @CanIgnoreReturnValue
    public HttpExecutionRequest body(final Object body) {
        return withEntity(FunctionUtils.doUnchecked(
            () -> MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(body)));
    }
}
