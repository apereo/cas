package org.apereo.cas.api;

/**
 * This is {@link PasswordlessRequestParser}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface PasswordlessRequestParser {

    String PARAMETER_USERNAME = "username";

    /**
     * Parse passwordless request.
     *
     * @param username the username
     * @return the passwordless request
     */
    PasswordlessRequest parse(String username);

    static PasswordlessRequestParser defaultParser() {
        return username -> PasswordlessRequest.builder().username(username).build();
    }
}
