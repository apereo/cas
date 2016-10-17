package org.apereo.cas.pm;

import com.google.common.collect.Maps;
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
    default boolean change(Credential c, PasswordChangeBean bean) {
        return false;
    }

    /**
     * Find email associated with username.
     *
     * @param username the username
     * @return the string
     */
    default String findEmail(String username) {
        return null;
    }

    /**
     * Create token string.
     *
     * @param username the username
     * @return the string
     */
    default String createToken(final String username) {
        return null;
    }

    /**
     * Parse token string.
     *
     * @param token the token
     * @return the username
     */
    default String parseToken(String token) {
        return null;
    }

    /**
     * Gets security questions.
     *
     * @param username the username
     * @return the security questions
     */
    default Map<String, String> getSecurityQuestions(final String username) {
        return Maps.newLinkedHashMap();
    }
}
