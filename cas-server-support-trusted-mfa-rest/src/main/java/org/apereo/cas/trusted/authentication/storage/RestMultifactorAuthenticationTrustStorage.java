package org.apereo.cas.trusted.authentication.storage;

import com.google.common.collect.Sets;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

/**
 * This is {@link RestMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RestMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {

    private String endpoint;

    public RestMultifactorAuthenticationTrustStorage(final String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final String principal) {
        final String url = (!this.endpoint.endsWith("/") ? this.endpoint.concat("/") : this.endpoint).concat(principal);
        final RestTemplate restTemplate = new RestTemplate();
        final ResponseEntity<MultifactorAuthenticationTrustRecord[]> responseEntity = 
                restTemplate.getForEntity(url, MultifactorAuthenticationTrustRecord[].class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            final MultifactorAuthenticationTrustRecord[] results = responseEntity.getBody();
            return Sets.newHashSet(results);
        }
        
        return Sets.newHashSet();
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
}
