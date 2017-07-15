package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

/**
 * This is {@link RestfulAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RestfulAuthenticationPolicy implements AuthenticationPolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestfulAuthenticationPolicy.class);

    private final RestTemplate restTemplate;
    private final String endpoint;

    public RestfulAuthenticationPolicy(final RestTemplate restTemplate, final String endpoint) {
        this.restTemplate = restTemplate;
        this.endpoint = endpoint;
    }

    @Override
    public boolean isSatisfiedBy(final Authentication authentication) throws Exception {
        return false;
    }
}
