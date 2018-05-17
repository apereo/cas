package org.apereo.cas.trusted.authentication.storage;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.HttpUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link RestMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class RestMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {

    private final RestTemplate restTemplate;
    private final CasConfigurationProperties properties;

    public RestMultifactorAuthenticationTrustStorage(final CasConfigurationProperties properties) {
        this.properties = properties;
        this.restTemplate = prepareRestTemplate();
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final String principal) {
        return getResults(getEndpointUrl(principal));
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final LocalDateTime onOrAfterDate) {
        return getResults(getEndpointUrl(onOrAfterDate.toString()));
    }

    @Override
    public void expire(final LocalDateTime onOrBefore) {
        final HttpEntity<Object> entity = getHttpEntity(onOrBefore);
        restTemplate.exchange(getEndpointUrl(null), HttpMethod.POST, entity, Object.class);
    }


    @Override
    public void expire(final String key) {
        final HttpEntity<Object> entity = getHttpEntity(key);
        restTemplate.exchange(getEndpointUrl(null), HttpMethod.POST, entity, Object.class);
    }

    @Override
    protected MultifactorAuthenticationTrustRecord setInternal(final MultifactorAuthenticationTrustRecord record) {
        final HttpEntity<Object> entity = getHttpEntity(record);
        final ResponseEntity<Object> response = restTemplate.exchange(getEndpointUrl(null), HttpMethod.POST, entity, Object.class);
        if (response != null && response.getStatusCode() == HttpStatus.OK) {
            return record;
        }
        return null;
    }

    private Set<MultifactorAuthenticationTrustRecord> getResults(final String url) {
        final HttpEntity<Object> entity = getHttpEntity(null);

        final ResponseEntity<MultifactorAuthenticationTrustRecord[]> responseEntity =
            restTemplate.exchange(url, HttpMethod.GET, entity, MultifactorAuthenticationTrustRecord[].class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            final MultifactorAuthenticationTrustRecord[] results = responseEntity.getBody();
            return Stream.of(results).collect(Collectors.toSet());
        }

        return new HashSet<>(0);
    }

    private HttpEntity<Object> getHttpEntity(final Object body) {
        final TrustedDevicesMultifactorProperties.Rest rest = properties.getAuthn().getMfa().getTrusted().getRest();
        return new HttpEntity<>(body, HttpUtils.createBasicAuthHeaders(rest.getBasicAuthUsername(), rest.getBasicAuthPassword()));
    }

    private String getEndpointUrl(final String path) {
        final String endpoint = properties.getAuthn().getMfa().getTrusted().getRest().getUrl();
        return (!endpoint.endsWith("/") ? endpoint.concat("/") : endpoint).concat(StringUtils.defaultString(path));
    }

    @SneakyThrows
    private RestTemplate prepareRestTemplate() {
        return new RestTemplate();
    }
}
