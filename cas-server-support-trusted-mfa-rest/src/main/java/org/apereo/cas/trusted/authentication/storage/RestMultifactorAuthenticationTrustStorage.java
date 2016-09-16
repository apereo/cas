package org.apereo.cas.trusted.authentication.storage;

import com.google.common.collect.Sets;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
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
        final RestTemplate restTemplate = new RestTemplate();
        final ResponseEntity<Object[]> responseEntity = restTemplate.getForEntity(
                this.endpoint.concat("/").concat(principal),
                Object[].class);
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
}
