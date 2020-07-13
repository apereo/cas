package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.model.support.mfa.gauth.GoogleAuthenticatorMultifactorProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import java.util.Map;
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
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
        .findAndRegisterModules();

    private final GoogleAuthenticatorMultifactorProperties gauth;

    public RestGoogleAuthenticatorTokenCredentialRepository(final IGoogleAuthenticator googleAuthenticator,
                                                            final GoogleAuthenticatorMultifactorProperties gauth,
                                                            final CipherExecutor<String, String> tokenCredentialCipher) {
        super(tokenCredentialCipher, googleAuthenticator);
        this.gauth = gauth;
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        val rest = gauth.getRest();
        HttpResponse response = null;
        try {
            response = HttpUtils.executeGet(rest.getUrl(), rest.getBasicAuthUsername(),
                rest.getBasicAuthUsername(), Map.of(),
                CollectionUtils.wrap("Accept", MediaType.APPLICATION_JSON));
            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.is2xxSuccessful()) {
                    val content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    if (content != null) {
                        final TypeReference<List<GoogleAuthenticatorAccount>> values = new TypeReference<>() {
                        };
                        val results = MAPPER.readValue(JsonValue.readHjson(content).toString(), values);
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
    public OneTimeTokenAccount get(final String username, final long id) {
        val rest = gauth.getRest();
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, Object>();
            response = HttpUtils.execute(rest.getUrl(), HttpMethod.GET.name(),
                rest.getBasicAuthUsername(), rest.getBasicAuthPassword(),
                parameters, Map.of("id", id, "username", username));

            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.is2xxSuccessful()) {
                    val content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    if (content != null) {
                        final TypeReference<GoogleAuthenticatorAccount> values = new TypeReference<>() {
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
    public OneTimeTokenAccount get(final long id) {
        val rest = gauth.getRest();
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, Object>();
            response = HttpUtils.execute(rest.getUrl(), HttpMethod.GET.name(),
                rest.getBasicAuthUsername(), rest.getBasicAuthPassword(),
                parameters, Map.of("id", id));

            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.is2xxSuccessful()) {
                    val content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    if (content != null) {
                        final TypeReference<GoogleAuthenticatorAccount> values = new TypeReference<>() {
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
            val parameters = new HashMap<String, Object>();
            response = HttpUtils.execute(rest.getUrl(), HttpMethod.GET.name(),
                rest.getBasicAuthUsername(), rest.getBasicAuthPassword(),
                parameters, Map.of("username", username));

            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.is2xxSuccessful()) {
                    val content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    if (content != null) {
                        final TypeReference<List<GoogleAuthenticatorAccount>> values = new TypeReference<>() {
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
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        return update(account);
    }

    @Override
    public void deleteAll() {
        val rest = gauth.getRest();
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, Object>();
            response = HttpUtils.execute(rest.getUrl(), HttpMethod.GET.name(),
                rest.getBasicAuthUsername(), rest.getBasicAuthPassword(),
                parameters, new HashMap<>(0));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void delete(final String username) {
        val rest = gauth.getRest();
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, Object>();
            response = HttpUtils.execute(rest.getUrl(), HttpMethod.GET.name(),
                rest.getBasicAuthUsername(), rest.getBasicAuthPassword(),
                parameters, CollectionUtils.wrap("Accept", MediaType.APPLICATION_JSON,
                    "username", username));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public long count() {
        val rest = gauth.getRest();
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, Object>();
            val countUrl = StringUtils.appendIfMissing(rest.getUrl(), "/").concat("count");
            response = HttpUtils.execute(countUrl, HttpMethod.GET.name(),
                rest.getBasicAuthUsername(), rest.getBasicAuthPassword(),
                parameters, CollectionUtils.wrap("Accept", MediaType.APPLICATION_JSON));

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
            val parameters = new HashMap<String, Object>();
            val countUrl = StringUtils.appendIfMissing(rest.getUrl(), "/").concat("count");
            response = HttpUtils.execute(countUrl, HttpMethod.GET.name(),
                rest.getBasicAuthUsername(), rest.getBasicAuthPassword(),
                parameters, CollectionUtils.wrap("Accept", MediaType.APPLICATION_JSON,
                    "username", username));

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

            val parameters = new HashMap<String, Object>();
            response = HttpUtils.execute(rest.getUrl(), HttpMethod.POST.name(),
                rest.getBasicAuthUsername(), rest.getBasicAuthPassword(),
                parameters, headers);

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
}
