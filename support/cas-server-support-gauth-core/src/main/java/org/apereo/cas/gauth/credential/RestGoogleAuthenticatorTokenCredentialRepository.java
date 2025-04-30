package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.model.support.mfa.gauth.GoogleAuthenticatorMultifactorProperties;
import org.apereo.cas.gauth.CasGoogleAuthenticator;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.http.HttpHeaders;
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

    public RestGoogleAuthenticatorTokenCredentialRepository(final CasGoogleAuthenticator googleAuthenticator,
                                                            final GoogleAuthenticatorMultifactorProperties gauth,
                                                            final CipherExecutor<String, String> tokenCredentialCipher,
                                                            final CipherExecutor<Number, Number> scratchCodesCipher) {
        super(tokenCredentialCipher, scratchCodesCipher, googleAuthenticator);
        this.gauth = gauth;
    }

    @Override
    public OneTimeTokenAccount get(final long id) {
        val rest = gauth.getRest();
        HttpResponse response = null;
        try {
            val headers = CollectionUtils.<String, String>wrap(
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
                "id", String.valueOf(id));
            headers.putAll(rest.getHeaders());
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(rest.getUrl())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getCode());
                if (status.is2xxSuccessful()) {
                    try (val contis = ((HttpEntityContainer) response).getEntity().getContent()) {
                        val content = IOUtils.toString(contis, StandardCharsets.UTF_8);
                        if (content != null) {
                            val values = new TypeReference<GoogleAuthenticatorAccount>() {
                            };
                            val result = MAPPER.readValue(JsonValue.readHjson(content).toString(), values);
                            return decode(Objects.requireNonNull(result));
                        }
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

            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
                "id", String.valueOf(id), "username", username);
            headers.putAll(rest.getHeaders());

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(rest.getUrl())
                .headers(headers)
                .build();

            response = HttpUtils.execute(exec);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getCode());
                if (status.is2xxSuccessful()) {
                    try (val contis = ((HttpEntityContainer) response).getEntity().getContent()) {
                        val content = IOUtils.toString(contis, StandardCharsets.UTF_8);
                        if (content != null) {
                            val values = new TypeReference<GoogleAuthenticatorAccount>() {
                            };
                            val result = MAPPER.readValue(JsonValue.readHjson(content).toString(), values);
                            return decode(Objects.requireNonNull(result));
                        }
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
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, "username", username);
            headers.putAll(rest.getHeaders());

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(rest.getUrl())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);

            if (response != null) {
                val status = HttpStatus.valueOf(response.getCode());
                if (status.is2xxSuccessful()) {
                    try (val contis = ((HttpEntityContainer) response).getEntity().getContent()) {
                        val content = IOUtils.toString(contis, StandardCharsets.UTF_8);
                        if (content != null) {
                            val values = new TypeReference<List<GoogleAuthenticatorAccount>>() {
                            };
                            val result = MAPPER.readValue(JsonValue.readHjson(content).toString(), values);
                            return decode(Objects.requireNonNull(result));
                        }
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
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(rest.getHeaders());

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(rest.getUrl())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getCode());
                if (status.is2xxSuccessful()) {
                    try (val contents = ((HttpEntityContainer) response).getEntity().getContent()) {
                        val content = IOUtils.toString(contents, StandardCharsets.UTF_8);
                        if (content != null) {
                            val results = MAPPER.readValue(JsonValue.readHjson(content).toString(),
                                    new TypeReference<List<GoogleAuthenticatorAccount>>() {
                                    });
                            return results.stream().map(this::decode).collect(Collectors.toList());
                        }
                    }
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return new ArrayList<>();
    }

    @Override
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        return update(account.assignIdIfNecessary());
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount accountToUpdate) {
        val rest = gauth.getRest();
        HttpResponse response = null;
        try {
            val account = encode(accountToUpdate);
            val headers = new HashMap<String, String>();
            headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.put("username", account.getUsername());
            headers.put("validationCode", String.valueOf(account.getValidationCode()));
            headers.put("secretKey", account.getSecretKey());

            val codes = account.getScratchCodes()
                .stream()
                .map(Number::toString)
                .collect(Collectors.joining(","));
            headers.put("scratchCodes", codes);

            headers.putAll(rest.getHeaders());

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.POST)
                .url(rest.getUrl())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);

            if (response != null) {
                val status = HttpStatus.valueOf(response.getCode());
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
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(rest.getUrl())
                .headers(rest.getHeaders())
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
            val headers = CollectionUtils.<String, String>wrap(
                HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE,
                "username", username);
            headers.putAll(rest.getHeaders());
            val exec = HttpExecutionRequest.builder()
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
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE, "id", id);
            headers.putAll(rest.getHeaders());
            val exec = HttpExecutionRequest.builder()
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
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(rest.getHeaders());
            val countUrl = StringUtils.appendIfMissing(rest.getUrl(), "/").concat("count");
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(countUrl)
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getCode());
                if (status.is2xxSuccessful()) {
                    try (val contis = ((HttpEntityContainer) response).getEntity().getContent()) {
                        val content = IOUtils.toString(contis, StandardCharsets.UTF_8);
                        if (content != null) {
                            return MAPPER.readValue(JsonValue.readHjson(content).toString(), Long.class);
                        }
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
            val headers = CollectionUtils.<String, String>wrap(
                HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE, "username", username);
            headers.putAll(rest.getHeaders());

            val countUrl = StringUtils.appendIfMissing(rest.getUrl(), "/").concat("count");
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(rest.getBasicAuthPassword())
                .basicAuthUsername(rest.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(countUrl)
                .headers(headers)
                .build();

            response = HttpUtils.execute(exec);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getCode());
                if (status.is2xxSuccessful()) {
                    try (val contis = ((HttpEntityContainer) response).getEntity().getContent()) {
                        val content = IOUtils.toString(contis, StandardCharsets.UTF_8);
                        if (content != null) {
                            return MAPPER.readValue(JsonValue.readHjson(content).toString(), Long.class);
                        }
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
