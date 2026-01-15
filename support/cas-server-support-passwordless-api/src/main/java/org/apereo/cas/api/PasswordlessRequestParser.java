package org.apereo.cas.api;
import module java.base;

/**
 * This is {@link PasswordlessRequestParser}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface PasswordlessRequestParser {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "passwordlessRequestParser";

    /**
     * Username parameter received from the request.
     */
    String PARAMETER_USERNAME = "username";

    /**
     * Parse passwordless request.
     *
     * @param username the username
     * @return the passwordless request
     */
    PasswordlessAuthenticationRequest parse(String username);

    /**
     * Default passwordless request parser.
     *
     * @return the passwordless request parser
     */
    static PasswordlessRequestParser defaultParser() {
        return username -> PasswordlessAuthenticationRequest.builder().username(username).build();
    }
}
