package org.apereo.cas.pm;

import org.apereo.cas.authentication.principal.WebApplicationService;

import java.net.URL;

/**
 * This is {@link PasswordResetUrlBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@FunctionalInterface
public interface PasswordResetUrlBuilder {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "passwordResetUrlBuilder";

    /**
     * Build reset URL.
     *
     * @param username the username
     * @param service  the service
     * @return the URL
     * @throws Exception the exception
     */
    URL build(String username, WebApplicationService service) throws Exception;
}
