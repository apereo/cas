package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This is {@link RestConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RestConsentRepository implements ConsentRepository {
    private static final long serialVersionUID = 6583408864586270206L;
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
    public Collection<ConsentDecision> findConsentDecisions(final String principal) {
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
            headers.put("principal", CollectionUtils.wrap(principal));

            final HttpEntity<String> entity = new HttpEntity<>(headers);
            final ResponseEntity<List> result = restTemplate.exchange(this.endpoint, HttpMethod.GET, entity, List.class);
            if (result.getStatusCodeValue() == HttpStatus.OK.value()) {
                return result.getBody();
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public Collection<ConsentDecision> findConsentDecisions() {
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));

            final HttpEntity<String> entity = new HttpEntity<>(headers);
            final ResponseEntity<List> result = restTemplate.exchange(this.endpoint, HttpMethod.GET, entity, List.class);
            if (result.getStatusCodeValue() == HttpStatus.OK.value()) {
                return result.getBody();
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public ConsentDecision findConsentDecision(final Service service,
                                               final RegisteredService registeredService,
                                               final Authentication authentication) {
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
            headers.put("service", CollectionUtils.wrap(service.getId()));
            headers.put("principal", CollectionUtils.wrap(authentication.getPrincipal().getId()));

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
            headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
            final HttpEntity<ConsentDecision> entity = new HttpEntity<>(decision, headers);
            final ResponseEntity<ConsentDecision> result = restTemplate.exchange(this.endpoint, HttpMethod.POST, entity, ConsentDecision.class);
            return result.getStatusCodeValue() == HttpStatus.OK.value();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
    
    @Override
    public boolean deleteConsentDecision(final long decisionId, final String principal) {
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));

            final HttpEntity<Map> entity = new HttpEntity<>(headers);
            final String deleteEndpoint = this.endpoint.concat("/" + Long.toString(decisionId));
            final ResponseEntity<Boolean> result = restTemplate.exchange(deleteEndpoint, HttpMethod.DELETE, entity, Boolean.class);
            return result.getStatusCodeValue() == HttpStatus.OK.value();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
