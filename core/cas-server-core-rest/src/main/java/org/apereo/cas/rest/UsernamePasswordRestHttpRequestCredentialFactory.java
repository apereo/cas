package org.apereo.cas.rest;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is {@link UsernamePasswordRestHttpRequestCredentialFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class UsernamePasswordRestHttpRequestCredentialFactory implements RestHttpRequestCredentialFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsernamePasswordRestHttpRequestCredentialFactory.class);
    
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Override
    public List<Credential> fromRequestBody(final Map<String, String> requestBody) {
        final String username = requestBody.get(USERNAME);
        final String password = requestBody.get(PASSWORD);
        if (username == null || password == null) {
            LOGGER.debug("Invalid payload. 'username' and 'password' form fields are required.");
            return new ArrayList<>(0);
        }
        final Credential c = new UsernamePasswordCredential(username, password);
        return CollectionUtils.wrap(c);
    }
}
