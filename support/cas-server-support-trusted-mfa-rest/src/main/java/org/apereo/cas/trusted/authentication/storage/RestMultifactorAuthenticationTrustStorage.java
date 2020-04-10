package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link RestMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RestMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    public RestMultifactorAuthenticationTrustStorage(final TrustedDevicesMultifactorProperties properties,
                                                     final CipherExecutor<Serializable, String> cipherExecutor,
                                                     final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy) {
        super(properties, cipherExecutor, keyGenerationStrategy);
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal) {
        return getResults(getEndpointUrl(principal));
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final ZonedDateTime onOrAfterDate) {
        return getResults(getEndpointUrl(onOrAfterDate.toString()));
    }

    @Override
    public MultifactorAuthenticationTrustRecord get(final long id) {
        val results = getResults(getEndpointUrl(String.valueOf(id)));
        return results.stream()
            .filter(entry -> entry.getId() == id)
            .sorted()
            .findFirst()
            .orElse(null);
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> getAll() {
        return getResults(getEndpointUrl(null));
    }

    @Override
    public void remove(final ZonedDateTime expirationDate) {
        val entity = getHttpEntity(expirationDate);
        val restTemplate = new RestTemplate();
        restTemplate.exchange(getEndpointUrl(null), HttpMethod.DELETE, entity, Object.class);
    }

    @Override
    public void remove(final String key) {
        val restTemplate = new RestTemplate();
        restTemplate.delete(getEndpointUrl(key));
    }

    @Override
    protected MultifactorAuthenticationTrustRecord saveInternal(final MultifactorAuthenticationTrustRecord record) {
        val entity = getHttpEntity(record);
        val restTemplate = new RestTemplate();
        val response = restTemplate.exchange(getEndpointUrl(null), HttpMethod.POST, entity, Object.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return record;
        }
        return null;
    }

    private Set<MultifactorAuthenticationTrustRecord> getResults(final String url) {
        val restTemplate = new RestTemplate();
        val entity = getHttpEntity(null);
        val responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, MultifactorAuthenticationTrustRecord[].class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            val results = responseEntity.getBody();
            return Stream.of(Objects.requireNonNull(results)).collect(Collectors.toSet());
        }
        return new HashSet<>(0);
    }

    private HttpEntity<Object> getHttpEntity(final Object body) {
        val rest = getTrustedDevicesMultifactorProperties().getRest();
        return new HttpEntity<>(body, HttpUtils.createBasicAuthHeaders(rest.getBasicAuthUsername(), rest.getBasicAuthPassword()));
    }

    private String getEndpointUrl(final String path) {
        val endpoint = getTrustedDevicesMultifactorProperties().getRest().getUrl();
        return (!endpoint.endsWith("/") ? endpoint.concat("/") : endpoint).concat(StringUtils.defaultString(path));
    }
}
