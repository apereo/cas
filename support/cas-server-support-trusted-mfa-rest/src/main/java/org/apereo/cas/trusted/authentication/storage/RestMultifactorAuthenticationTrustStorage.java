package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
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
public class RestMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {

    private final String endpoint;

    public RestMultifactorAuthenticationTrustStorage(final String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final String principal) {
        final String url = (!this.endpoint.endsWith("/") ? this.endpoint.concat("/") : this.endpoint).concat(principal);
        return getResults(url);
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final LocalDate onOrAfterDate) {
        final String url = (!this.endpoint.endsWith("/") ? this.endpoint.concat("/") : this.endpoint).concat(onOrAfterDate.toString());
        return getResults(url);
    }
    
    @Override
    public void expire(final LocalDate onOrBefore) {
        final RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForEntity(this.endpoint, onOrBefore, Object.class);
    }

    @Override
    public void expire(final String key) {
        final RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForEntity(this.endpoint, key, Object.class);
    }

    @Override
    protected MultifactorAuthenticationTrustRecord setInternal(final MultifactorAuthenticationTrustRecord record) {
        final RestTemplate restTemplate = new RestTemplate();
        final ResponseEntity<Object> response = restTemplate.postForEntity(this.endpoint, record, Object.class);
        if (response != null && response.getStatusCode() == HttpStatus.OK) {
            return record;
        }
        return null;
    }
    
    private static Set<MultifactorAuthenticationTrustRecord> getResults(final String url) {
        final RestTemplate restTemplate = new RestTemplate();
        final ResponseEntity<MultifactorAuthenticationTrustRecord[]> responseEntity =
                restTemplate.getForEntity(url, MultifactorAuthenticationTrustRecord[].class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            final MultifactorAuthenticationTrustRecord[] results = responseEntity.getBody();
            return Stream.of(results).collect(Collectors.toSet());
        }

        return new HashSet<>(0);
    }
}
