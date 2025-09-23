package org.apereo.cas.pm;

import org.apereo.cas.authentication.Credential;

import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link PasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface PasswordManagementService {

    /**
     * Name of parameter that can be supplied to login url to force display of password change during login.
     */
    String PARAMETER_DO_CHANGE_PASSWORD = "doChangePassword";

    /**
     * Param name for the token.
     */
    String PARAMETER_PASSWORD_RESET_TOKEN = "pswdrst";

    /**
     * FlowScope param name for token.
     */
    String PARAMETER_TOKEN = "token";

    /**
     * Default bean name for implementation.
     */
    String DEFAULT_BEAN_NAME = "passwordChangeService";

    /**
     * Execute op to change password.
     *
     * @param bean the bean
     * @return true /false
     * @throws Throwable the throwable
     */
    default boolean change(final PasswordChangeRequest bean) throws Throwable {
        return false;
    }

    /**
     * Unlock account for credential.
     *
     * @param credential the credential
     * @return true /false
     * @throws Throwable the throwable
     */
    default boolean unlockAccount(final Credential credential) throws Throwable {
        return false;
    }

    /**
     * Find email associated with username.
     *
     * @param query the username
     * @return the string
     * @throws Throwable the throwable
     */
    default String findEmail(final PasswordManagementQuery query) throws Throwable {
        return null;
    }

    /**
     * Find phone associated with username.
     *
     * @param query the query
     * @return the string
     * @throws Throwable the throwable
     */
    default String findPhone(final PasswordManagementQuery query) throws Throwable {
        return null;
    }

    /**
     * Find username linked to the email.
     *
     * @param query the query
     * @return the string
     * @throws Throwable the throwable
     */
    default String findUsername(final PasswordManagementQuery query) throws Throwable {
        return null;
    }

    /**
     * Create token string.
     *
     * @param query the query
     * @return the string
     */
    default String createToken(final PasswordManagementQuery query) {
        return null;
    }

    /**
     * Parse token string.
     *
     * @param token the token
     * @return the username
     */
    default String parseToken(final String token) {
        return null;
    }

    /**
     * Gets security questions.
     * <p>
     * The return object must have predictable iteration (use LinkedHashMap
     * instead of HashMap, for example).
     *
     * @param query the query
     * @return the security questions
     * @throws Throwable the throwable
     */
    default Map<String, String> getSecurityQuestions(final PasswordManagementQuery query) throws Throwable {
        return new LinkedHashMap<>();
    }

    /**
     * Update security questions.
     *
     * @param query the query
     * @throws Throwable the throwable
     */
    default void updateSecurityQuestions(final PasswordManagementQuery query) throws Throwable {
    }

    /**
     * Checks a security questions answer.
     *
     * @param query    the query
     * @param question the text of the question
     * @param knownAnswer   stored answer
     * @param givenAnswer    user response to question
     * @return whether the answer is correct
     */
    default boolean isAnswerValidForSecurityQuestion(final PasswordManagementQuery query, final String question,
                                                     final String knownAnswer, final String givenAnswer) {
        return StringUtils.isNotBlank(knownAnswer) && knownAnswer.equals(givenAnswer);
    }

    /**
     * Orders security questions consistently.
     *
     * @param questionMap A map of question/answer key/value pairs
     * @return A list of questions in a consistent order
     */
    static List<String> canonicalizeSecurityQuestions(final Map<String, String> questionMap) {
        val keys = new ArrayList<>(questionMap.keySet());
        keys.sort(String.CASE_INSENSITIVE_ORDER);
        return keys;
    }
}
