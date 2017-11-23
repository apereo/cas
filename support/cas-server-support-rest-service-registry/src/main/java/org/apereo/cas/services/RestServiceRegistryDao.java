package org.apereo.cas.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link RestServiceRegistryDao}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RestServiceRegistryDao extends AbstractServiceRegistryDao {
    private final RestTemplate restTemplate;
    private final String url;
    private final MultiValueMap<String, String> headers;

    public RestServiceRegistryDao(final RestTemplate restTemplate, final String url, final MultiValueMap<String, String> headers) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.headers = headers;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        final ResponseEntity<RegisteredService> responseEntity = restTemplate.exchange(this.url, HttpMethod.POST,
                new HttpEntity<>(registeredService, this.headers), RegisteredService.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        }
        return null;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        final ResponseEntity<Integer> responseEntity = restTemplate.exchange(this.url, HttpMethod.DELETE,
                new HttpEntity<>(registeredService, this.headers), Integer.class);
        return responseEntity.getStatusCode() == HttpStatus.OK;
    }

    @Override
    public List<RegisteredService> load() {
        final ResponseEntity<RegisteredService[]> responseEntity = restTemplate.exchange(this.url, HttpMethod.GET,
                new HttpEntity<>(this.headers), RegisteredService[].class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            final RegisteredService[] results = responseEntity.getBody();
            return Stream.of(results).collect(Collectors.toList());
        }
        return new ArrayList<>(0);
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        final ResponseEntity<RegisteredService> responseEntity = restTemplate.exchange(this.url, HttpMethod.GET,
                new HttpEntity<>(id, this.headers), RegisteredService.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        }
        return null;
    }

    @Override
    public RegisteredService findServiceById(final String id) {
        final ResponseEntity<RegisteredService> responseEntity = restTemplate.exchange(this.url, HttpMethod.GET,
                new HttpEntity<>(id, this.headers), RegisteredService.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        }
        return null;
    }

    @Override
    public long size() {
        return load().size();
    }
}
