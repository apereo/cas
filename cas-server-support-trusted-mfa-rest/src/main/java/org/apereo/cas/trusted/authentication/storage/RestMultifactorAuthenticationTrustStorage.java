package org.apereo.cas.trusted.authentication.storage;

import com.google.common.collect.Sets;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

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
            final Object[] results = responseEntity.getBody();
            return Arrays.stream(results)
                    .map(e -> MultifactorAuthenticationTrustRecord.class.cast(e))
                    .collect(Collectors.toSet());
        }
        return Sets.newHashSet();
    }

    @Override
    protected MultifactorAuthenticationTrustRecord setInternal(final MultifactorAuthenticationTrustRecord record) {
        final RestTemplate restTemplate = new RestTemplate();
        final Integer status = restTemplate.postForObject(this.endpoint, record, Integer.class);
        if (status == HttpStatus.OK.value()) {
            return record;
        }
        return null;
    }

    public static void main(final String[] args) {
        RestMultifactorAuthenticationTrustStorage r = new RestMultifactorAuthenticationTrustStorage(
                "http://demo5926981.mockable.io/"
        );
        r.setCipherExecutor(new NoOpCipherExecutor());
        r.get("casuser");
    }
}
