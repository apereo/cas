package org.apereo.cas.impl.token;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.configuration.model.support.passwordless.token.PasswordlessAuthenticationRestTokensProperties;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.http.HttpMethod;

/**
 * This is {@link RestfulPasswordlessTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class RestfulPasswordlessTokenRepository extends BasePasswordlessTokenRepository {
    private final PasswordlessAuthenticationRestTokensProperties restProperties;

    public RestfulPasswordlessTokenRepository(final long tokenExpirationInSeconds,
                                              final PasswordlessAuthenticationRestTokensProperties restProperties,
                                              final CipherExecutor<Serializable, String> cipherExecutor) {
        super(tokenExpirationInSeconds, cipherExecutor);
        this.restProperties = restProperties;
    }

    @Override
    public Optional<PasswordlessAuthenticationToken> findToken(final String username) {
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, String>();
            parameters.put("username", username);
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .maximumRetryAttempts(restProperties.getMaximumRetryAttempts())
                .method(HttpMethod.GET)
                .url(restProperties.getUrl())
                .headers(restProperties.getHeaders())
                .parameters(parameters)
                .build();

            response = HttpUtils.execute(exec);
            if (response != null && ((HttpEntityContainer) response).getEntity() != null) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val token = IOUtils.toString(content, StandardCharsets.UTF_8);
                    val result = decodePasswordlessAuthenticationToken(token);
                    return Optional.of(result);
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return Optional.empty();
    }

    @Override
    public void deleteTokens(final String username) {
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, String>();
            parameters.put("username", username);
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .maximumRetryAttempts(restProperties.getMaximumRetryAttempts())
                .method(HttpMethod.DELETE)
                .url(restProperties.getUrl())
                .headers(restProperties.getHeaders())
                .parameters(parameters)
                .build();
            response = HttpUtils.execute(exec);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void deleteToken(final PasswordlessAuthenticationToken token) {
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, String>();
            parameters.put("username", token.getUsername());
            parameters.put("token", encodeToken(token));
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .maximumRetryAttempts(restProperties.getMaximumRetryAttempts())
                .method(HttpMethod.DELETE)
                .url(restProperties.getUrl())
                .headers(restProperties.getHeaders())
                .parameters(parameters)
                .build();
            response = HttpUtils.execute(exec);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public PasswordlessAuthenticationToken saveToken(final PasswordlessUserAccount passwordlessAccount,
                                                     final PasswordlessAuthenticationRequest passwordlessRequest,
                                                     final PasswordlessAuthenticationToken authnToken) {
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, String>();
            parameters.put("username", passwordlessAccount.getUsername());
            parameters.put("token", encodeToken(authnToken));
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .maximumRetryAttempts(restProperties.getMaximumRetryAttempts())
                .method(HttpMethod.POST)
                .headers(restProperties.getHeaders())
                .url(restProperties.getUrl())
                .parameters(parameters)
                .build();
            response = HttpUtils.execute(exec);
            return authnToken;
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void clean() {
        HttpResponse response = null;
        try {
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .maximumRetryAttempts(restProperties.getMaximumRetryAttempts())
                .method(HttpMethod.DELETE)
                .url(restProperties.getUrl())
                .headers(restProperties.getHeaders())
                .build();
            response = HttpUtils.execute(exec);
        } finally {
            HttpUtils.close(response);
        }
    }
}
