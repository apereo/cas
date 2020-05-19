package org.apereo.cas.impl.token;

import org.apereo.cas.configuration.model.support.passwordless.token.PasswordlessAuthenticationRestTokensProperties;
import org.apereo.cas.util.HttpUtils;
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
            response = HttpUtils.execute(restProperties.getUrl(), HttpMethod.GET.name(),
                restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword(),
                parameters, new HashMap<>(0));
            if (response != null && response.getEntity() != null) {
                val token = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                val result = cipherExecutor.decode(token).toString();
                return Optional.of(result);
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
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
            response = HttpUtils.execute(restProperties.getUrl(), HttpMethod.DELETE.name(),
                restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword(),
                parameters, new HashMap<>(0));
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
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
            response = HttpUtils.execute(restProperties.getUrl(), HttpMethod.DELETE.name(),
                restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword(),
                parameters, new HashMap<>(0));
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
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
            response = HttpUtils.execute(restProperties.getUrl(), HttpMethod.POST.name(),
                restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword(),
                parameters, new HashMap<>(0));
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void clean() {
        HttpResponse response = null;
        try {
            response = HttpUtils.execute(restProperties.getUrl(), HttpMethod.DELETE.name(),
                restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword(),
                new HashMap<>(0), new HashMap<>(0));
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        } finally {
            HttpUtils.close(response);
        }
    }
}
