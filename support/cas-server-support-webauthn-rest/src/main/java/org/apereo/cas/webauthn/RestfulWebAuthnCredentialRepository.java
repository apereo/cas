package org.apereo.cas.webauthn;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.webauthn.storage.BaseWebAuthnCredentialRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yubico.data.CredentialRegistration;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * This is {@link RestfulWebAuthnCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
public class RestfulWebAuthnCredentialRepository extends BaseWebAuthnCredentialRepository {
    public RestfulWebAuthnCredentialRepository(final CasConfigurationProperties properties,
                                               final CipherExecutor<String, String> cipherExecutor) {
        super(properties, cipherExecutor);
    }

    @Override
    public Collection<CredentialRegistration> getRegistrationsByUsername(final String username) {
        val restProperties = getProperties().getAuthn().getMfa().getWebAuthn().getRest();
        HttpResponse response = null;
        try {
            val parameters = CollectionUtils.<String, String>wrap("username", username);
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(restProperties.getUrl())
                .parameters(parameters)
                .headers(restProperties.getHeaders())
                .maximumRetryAttempts(restProperties.getMaximumRetryAttempts())
                .build();
            response = HttpUtils.execute(exec);
            if (Objects.requireNonNull(response).getCode() == HttpStatus.OK.value()) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val result = getCipherExecutor().decode(IOUtils.toString(content, StandardCharsets.UTF_8));
                    return WebAuthnUtils.getObjectMapper().readValue(result, new TypeReference<List<CredentialRegistration>>() {
                    });
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
    public Stream<CredentialRegistration> stream() {
        val restProperties = getProperties().getAuthn().getMfa().getWebAuthn().getRest();
        HttpResponse response = null;
        try {
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(restProperties.getUrl())
                .headers(restProperties.getHeaders())
                .maximumRetryAttempts(restProperties.getMaximumRetryAttempts())
                .build();
            response = HttpUtils.execute(exec);
            if (Objects.requireNonNull(response).getCode() == HttpStatus.OK.value()) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val result = getCipherExecutor().decode(IOUtils.toString(content, StandardCharsets.UTF_8));
                    val records = WebAuthnUtils.getObjectMapper().readValue(result, new TypeReference<List<CredentialRegistration>>() {
                    });
                    return records.stream();
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return Stream.empty();
    }

    @Override
    protected void update(final String username, final Collection<CredentialRegistration> records) {
        val restProperties = getProperties().getAuthn().getMfa().getWebAuthn().getRest();
        HttpResponse response = null;
        try {
            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(restProperties.getHeaders());
            val parameters = CollectionUtils.<String, String>wrap("username", username);
            val jsonRecords = FunctionUtils.doUnchecked(() -> getCipherExecutor().encode(WebAuthnUtils.getObjectMapper().writeValueAsString(records)));
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.POST)
                .url(restProperties.getUrl())
                .entity(jsonRecords)
                .headers(headers)
                .parameters(parameters)
                .maximumRetryAttempts(restProperties.getMaximumRetryAttempts())
                .build();
            response = HttpUtils.execute(exec);
        } finally {
            HttpUtils.close(response);
        }
    }
}
