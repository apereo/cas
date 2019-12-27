package org.apereo.cas.rest.factory;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;

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
@Setter
public class UsernamePasswordRestHttpRequestCredentialFactory implements RestHttpRequestCredentialFactory {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private int order = Integer.MAX_VALUE;

    @Override
    public List<Credential> fromRequest(final HttpServletRequest request, final MultiValueMap<String, String> requestBody) {
        if (requestBody == null || requestBody.isEmpty()) {
            LOGGER.debug("Skipping [{}] because the requestBody is null or empty", getClass().getSimpleName());
            return new ArrayList<>(0);
        }
        val username = requestBody.getFirst(USERNAME);
        val password = requestBody.getFirst(PASSWORD);
        if (username == null || password == null) {
            LOGGER.debug("Invalid payload. 'username' and 'password' form fields are required.");
            return new ArrayList<>(0);
        }
        val c = new UsernamePasswordCredential(username, password);
        return CollectionUtils.wrap(c);
    }
}
