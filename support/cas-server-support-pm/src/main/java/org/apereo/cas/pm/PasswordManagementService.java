package org.apereo.cas.pm;

import org.apereo.cas.authentication.Credential;

import java.util.LinkedHashMap;
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
     * @throws InvalidPasswordException if new password fails downstream validation
     */
    default boolean change(Credential c, PasswordChangeBean bean) throws InvalidPasswordException {
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
    default String createToken(String username) {
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
     * The return object must have predictable iteration (use LinkedHashMap
     * instead of HashMap, for example).
     * 
     * @param username the username
     * @return the security questions
     */
    default Map<String, String> getSecurityQuestions(String username) {
        return new LinkedHashMap<>(0);
    }

    /**
     * Checks a security questions answer.
     *
     * @param username the username
     * @param question the text of the question
     * @param answer stored answer
     * @param input user response to question
     * @return whether the answer is correct
     */
    default boolean isValidSecurityQuestionAnswer(String username, String question, String answer, String input) {
        if (answer != null) {
            return answer.equals(input);
        }
        return false;
    }
}
