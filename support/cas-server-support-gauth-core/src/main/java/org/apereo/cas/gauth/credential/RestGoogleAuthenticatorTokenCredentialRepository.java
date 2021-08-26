package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.model.support.mfa.gauth.GoogleAuthenticatorMultifactorProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link RestGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
public class RestGoogleAuthenticatorTokenCredentialRepository extends BaseGoogleAuthenticatorTokenCredentialRepository {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).singleValueAsArray(true).build().toObjectMapper();

    private final GoogleAuthenticatorMultifactorProperties gauth;

    public RestGoogleAuthenticatorTokenCredentialRepository(final IGoogleAuthenticator googleAuthenticator,
        final GoogleAuthenticatorMultifactorProperties gauth,
        final CipherExecutor<String, String> tokenCredentialCipher) {
        super(tokenCredentialCipher, googleAuthenticator);
        this.gauth = gauth;
    }

    @Override
    public OneTimeTokenAccount get(final long id) {
        val rest = gauth.getRest();
        HttpResponse response = null;
        try {
            val headers = CollectionUtils.<String, Object>wrap("Content-Type", MediaType.APPLICATION_JSON_VALUE, "id", id);
            headers.putAll(rest.getHeaders());
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(rest.getUrl())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.is2xxSuccessful()) {
                    val content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    if (content != null) {
                        val values = new TypeReference<GoogleAuthenticatorAccount>() {
                        };
                        val result = MAPPER.readValue(JsonValue.readHjson(content).toString(), values);
                        return decode(Objects.requireNonNull(result));
                    }
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return null;
    }

    @Override
    public OneTimeTokenAccount get(final String username, final long id) {
        HttpResponse response = null;
        try {
            val rest = gauth.getRest();

            val headers = CollectionUtils.<String, Object>wrap("Content-Type", MediaType.APPLICATION_JSON_VALUE,
                "id", id, "username", username);
            headers.putAll(rest.getHeaders());

            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(rest.getUrl())
                .headers(headers)
                .build();

            response = HttpUtils.execute(exec);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.is2xxSuccessful()) {
                    val content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    if (content != null) {
                        val values = new TypeReference<GoogleAuthenticatorAccount>() {
                        };
                        val result = MAPPER.readValue(JsonValue.readHjson(content).toString(), values);
                        return decode(Objects.requireNonNull(result));
                    }
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return null;
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> get(final String username) {
        val rest = gauth.getRest();
        HttpResponse response = null;
        try {
            val headers = CollectionUtils.<String, Object>wrap("Content-Type", MediaType.APPLICATION_JSON_VALUE, "username", username);
            headers.putAll(rest.getHeaders());

            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(rest.getUrl())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);

            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.is2xxSuccessful()) {
                    val content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    if (content != null) {
                        val values = new TypeReference<List<GoogleAuthenticatorAccount>>() {
                        };
                        val result = MAPPER.readValue(JsonValue.readHjson(content).toString(), values);
                        return decode(Objects.requireNonNull(result));
                    }
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return null;
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        val rest = gauth.getRest();
        HttpResponse response = null;
        try {
            val headers = CollectionUtils.<String, Object>wrap("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(rest.getHeaders());

            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(rest.getUrl())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.is2xxSuccessful()) {
                    val content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    if (content != null) {
                        val results = MAPPER.readValue(JsonValue.readHjson(content).toString(),
                            new TypeReference<List<GoogleAuthenticatorAccount>>() {
                            });
                        return results.stream().map(this::decode).collect(Collectors.toList());
                    }
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return new ArrayList<>(0);
    }

    @Override
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        return update(account);
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount accountToUpdate) {
        val rest = gauth.getRest();
        HttpResponse response = null;
        try {
            val account = encode(accountToUpdate);
            val headers = new HashMap<String, Object>();
            headers.put("Accept", MediaType.APPLICATION_JSON);
            headers.put("username", CollectionUtils.wrap(account.getUsername()));
            headers.put("validationCode", CollectionUtils.wrap(String.valueOf(account.getValidationCode())));
            headers.put("secretKey", CollectionUtils.wrap(account.getSecretKey()));
            headers.put("scratchCodes", account.getScratchCodes().stream().map(String::valueOf).collect(Collectors.toList()));

            headers.putAll(rest.getHeaders());

            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.POST)
                .url(rest.getUrl())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);

            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.is2xxSuccessful()) {
                    LOGGER.debug("Posted google authenticator account successfully");
                    return account;
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        LOGGER.warn("Failed to save google authenticator account successfully");
        return null;
    }

    @Override
    public void deleteAll() {
        val rest = gauth.getRest();
        HttpResponse response = null;
        try {
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(rest.getUrl())
                .build();
            response = HttpUtils.execute(exec);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void delete(final String username) {
        val rest = gauth.getRest();
        HttpResponse response = null;
        try {
            val headers = CollectionUtils.wrap("Accept", MediaType.APPLICATION_JSON_VALUE, "username", username);
            headers.putAll(rest.getHeaders());
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(rest.getUrl())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void delete(final long id) {
        val rest = gauth.getRest();
        HttpResponse response = null;
        try {
            val headers = CollectionUtils.wrap("Accept", MediaType.APPLICATION_JSON_VALUE, "id", id);
            headers.putAll(rest.getHeaders());
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(rest.getUrl())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public long count() {
        val rest = gauth.getRest();
        HttpResponse response = null;
        try {
            val headers = CollectionUtils.<String, Object>wrap("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(rest.getHeaders());
            val countUrl = StringUtils.appendIfMissing(rest.getUrl(), "/").concat("count");
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(countUrl)
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.is2xxSuccessful()) {
                    val content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    if (content != null) {
                        return MAPPER.readValue(JsonValue.readHjson(content).toString(), Long.class);
                    }
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return 0;
    }

    @Override
    public long count(final String username) {
        val rest = gauth.getRest();
        HttpResponse response = null;
        try {
            val headers = CollectionUtils.<String, Object>wrap("Accept", MediaType.APPLICATION_JSON_VALUE, "username", username);
            headers.putAll(rest.getHeaders());

            val countUrl = StringUtils.appendIfMissing(rest.getUrl(), "/").concat("count");
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(countUrl)
                .headers(headers)
                .build();

            response = HttpUtils.execute(exec);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.is2xxSuccessful()) {
                    val content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    if (content != null) {
                        return MAPPER.readValue(JsonValue.readHjson(content).toString(), Long.class);
                    }
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return 0;
    }
}
