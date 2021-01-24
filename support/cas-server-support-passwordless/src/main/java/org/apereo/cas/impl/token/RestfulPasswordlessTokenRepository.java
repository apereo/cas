package org.apereo.cas.impl.token;

import org.apereo.cas.configuration.model.support.passwordless.token.PasswordlessAuthenticationRestTokensProperties;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.springframework.http.HttpMethod;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;

/**
 * This is {@link RestfulPasswordlessTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class RestfulPasswordlessTokenRepository extends BasePasswordlessTokenRepository {
    private final PasswordlessAuthenticationRestTokensProperties restProperties;

    private final CipherExecutor cipherExecutor;

    public RestfulPasswordlessTokenRepository(final int tokenExpirationInSeconds,
                                              final PasswordlessAuthenticationRestTokensProperties restProperties,
                                              final CipherExecutor<Serializable, String> cipherExecutor) {
        super(tokenExpirationInSeconds);
        this.restProperties = restProperties;
        this.cipherExecutor = cipherExecutor;
    }

    @Override
    public Optional<String> findToken(final String username) {
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, Object>();
            parameters.put("username", username);
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(restProperties.getUrl())
                .parameters(parameters)
                .build();

            response = HttpUtils.execute(exec);
            if (response != null && response.getEntity() != null) {
                val token = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                val result = cipherExecutor.decode(token).toString();
                return Optional.of(result);
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
            val parameters = new HashMap<String, Object>();
            parameters.put("username", username);
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.DELETE)
                .url(restProperties.getUrl())
                .parameters(parameters)
                .build();
            response = HttpUtils.execute(exec);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void deleteToken(final String username, final String token) {
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, Object>();
            parameters.put("username", username);
            parameters.put("token", cipherExecutor.encode(token).toString());
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.DELETE)
                .url(restProperties.getUrl())
                .parameters(parameters)
                .build();
            response = HttpUtils.execute(exec);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void saveToken(final String username, final String token) {
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, Object>();
            parameters.put("username", username);
            parameters.put("token", cipherExecutor.encode(token).toString());
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.POST)
                .url(restProperties.getUrl())
                .parameters(parameters)
                .build();
            response = HttpUtils.execute(exec);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void clean() {
        HttpResponse response = null;
        try {
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.DELETE)
                .url(restProperties.getUrl())
                .build();
            response = HttpUtils.execute(exec);
        } finally {
            HttpUtils.close(response);
        }
    }
}
