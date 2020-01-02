package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.HttpUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
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
@RequiredArgsConstructor
public class RestMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    private final RestTemplate restTemplate;
    private final CasConfigurationProperties properties;

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal) {
        return getResults(getEndpointUrl(principal));
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final LocalDateTime onOrAfterDate) {
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
    public void expire(final LocalDateTime onOrBefore) {
        val entity = getHttpEntity(onOrBefore);
        restTemplate.exchange(getEndpointUrl(null), HttpMethod.POST, entity, Object.class);
    }

    @Override
    public void expire(final String key) {
        val entity = getHttpEntity(key);
        restTemplate.exchange(getEndpointUrl(null), HttpMethod.POST, entity, Object.class);
    }

    @Override
    protected MultifactorAuthenticationTrustRecord setInternal(final MultifactorAuthenticationTrustRecord record) {
        val entity = getHttpEntity(record);
        val response = restTemplate.exchange(getEndpointUrl(null), HttpMethod.POST, entity, Object.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return record;
        }
        return null;
    }

    private Set<MultifactorAuthenticationTrustRecord> getResults(final String url) {
        val entity = getHttpEntity(null);
        val responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, MultifactorAuthenticationTrustRecord[].class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            val results = responseEntity.getBody();
            return Stream.of(Objects.requireNonNull(results)).collect(Collectors.toSet());
        }
        return new HashSet<>(0);
    }

    private HttpEntity<Object> getHttpEntity(final Object body) {
        val rest = properties.getAuthn().getMfa().getTrusted().getRest();
        return new HttpEntity<>(body, HttpUtils.createBasicAuthHeaders(rest.getBasicAuthUsername(), rest.getBasicAuthPassword()));
    }

    private String getEndpointUrl(final String path) {
        val endpoint = properties.getAuthn().getMfa().getTrusted().getRest().getUrl();
        return (!endpoint.endsWith("/") ? endpoint.concat("/") : endpoint).concat(StringUtils.defaultString(path));
    }
}
