package org.apereo.cas.support.rest.factory;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.support.rest.BadRequestException;
import org.apereo.cas.support.rest.CredentialFactory;
import org.springframework.util.MultiValueMap;

/**
 * This is {@link DefaultCredentialFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultCredentialFactory implements CredentialFactory {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Override
    public Credential fromRequestBody(final MultiValueMap<String, String> requestBody) {
        final String username = requestBody.getFirst(USERNAME);
        final String password = requestBody.getFirst(PASSWORD);
        if (username == null || password == null) {
            throw new BadRequestException("Invalid payload. 'username' and 'password' form fields are required.");
        }
        return new UsernamePasswordCredential(requestBody.getFirst(USERNAME),
                requestBody.getFirst(PASSWORD));
    }
}
