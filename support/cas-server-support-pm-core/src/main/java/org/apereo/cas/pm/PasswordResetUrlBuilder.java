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
     * @throws Throwable the throwable
     */
    URL build(String username, WebApplicationService service) throws Throwable;

    /**
     * Build url without service.
     *
     * @param username the username
     * @return the url
     * @throws Throwable the throwable
     */
    default URL build(final String username) throws Throwable {
        return build(username, null);
    }
}
