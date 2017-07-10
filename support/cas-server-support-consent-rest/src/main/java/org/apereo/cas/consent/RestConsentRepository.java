package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * This is {@link RestConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RestConsentRepository implements ConsentRepository {
    private static final long serialVersionUID = 6583408862493270206L;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestConsentRepository.class);

    private final RestTemplate restTemplate;
    private final String endpoint;

    public RestConsentRepository(final RestTemplate restTemplate, final String endpoint) {
        this.restTemplate = restTemplate;
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public ConsentDecision findConsentDecision(final Service service,
                                               final RegisteredService registeredService,
                                               final Authentication authentication) {
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.put("service", Arrays.asList(service.getId()));
            headers.put("principal", Arrays.asList(authentication.getPrincipal().getId()));

            final HttpEntity<String> entity = new HttpEntity<>(headers);
            final ResponseEntity<ConsentDecision> result = restTemplate.exchange(this.endpoint, HttpMethod.GET, entity, ConsentDecision.class);
            if (result.getStatusCodeValue() == HttpStatus.OK.value()) {
                return result.getBody();
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean storeConsentDecision(final ConsentDecision decision) {
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            final HttpEntity<ConsentDecision> entity = new HttpEntity<>(decision, headers);
            final ResponseEntity<ConsentDecision> result = restTemplate.exchange(this.endpoint, HttpMethod.POST, entity, ConsentDecision.class);
            return result.getStatusCodeValue() == HttpStatus.OK.value();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
