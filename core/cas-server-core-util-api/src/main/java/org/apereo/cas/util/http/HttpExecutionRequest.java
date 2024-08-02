package org.apereo.cas.util.http;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
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
public class HttpExecutionRequest {
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
}
