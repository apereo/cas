package org.apereo.cas.rest;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link UsernamePasswordRestHttpRequestCredentialFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
public class UsernamePasswordRestHttpRequestCredentialFactory implements RestHttpRequestCredentialFactory {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private int order;

    @Override
    public List<Credential> fromRequestBody(final MultiValueMap<String, String> requestBody) {
        final String username = requestBody.getFirst(USERNAME);
        final String password = requestBody.getFirst(PASSWORD);
        if (username == null || password == null) {
            LOGGER.debug("Invalid payload. 'username' and 'password' form fields are required.");
            return new ArrayList<>(0);
        }
        final Credential c = new UsernamePasswordCredential(username, password);
        return CollectionUtils.wrap(c);
    }
}
