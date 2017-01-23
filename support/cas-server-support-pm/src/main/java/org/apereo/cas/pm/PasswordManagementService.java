package org.apereo.cas.pm;

import org.apereo.cas.authentication.Credential;

import java.util.Map;

/**
 * This is {@link PasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface PasswordManagementService {

    /**
     * Execute op to change password.
     *
     * @param c    the credentials
     * @param bean the bean
     * @return true /false
     */
    boolean change(Credential c, PasswordChangeBean bean);

    /**
     * Find email associated with username.
     *
     * @param username the username
     * @return the string
     */
    String findEmail(String username);

    /**
     * Create token string.
     *
     * @param username the username
     * @return the string
     */
    String createToken(String username);

    /**
     * Parse token string.
     *
     * @param token the token
     * @return the username
     */
    String parseToken(String token);

    /**
     * Gets security questions.
     *
     * @param username the username
     * @return the security questions
     */
    Map<String, String> getSecurityQuestions(String username);
}
