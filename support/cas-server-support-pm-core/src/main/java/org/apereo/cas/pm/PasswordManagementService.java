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
     * Param name for the token.
     */
    String PARAMETER_PASSWORD_RESET_TOKEN = "pswdrst";

    /**
     * Flowscope param name for token.
     */
    String PARAMETER_TOKEN = "token";

    /**
     * Default bean name for implementation.
     */
    String DEFAULT_BEAN_NAME = "passwordChangeService";

    /**
     * Execute op to change password.
     *
     * @param credential the credentials
     * @param bean       the bean
     * @return true /false
     * @throws InvalidPasswordException if new password fails downstream validation
     */
    default boolean change(final Credential credential, final PasswordChangeRequest bean) throws InvalidPasswordException {
        return false;
    }

    /**
     * Unlock account for credential.
     *
     * @param credential the credential
     * @return true/false
     */
    default boolean unlockAccount(final Credential credential) {
        return false;
    }

    /**
     * Find email associated with username.
     *
     * @param query the username
     * @return the string
     */
    default String findEmail(final PasswordManagementQuery query) {
        return null;
    }

    /**
     * Find phone associated with username.
     *
     * @param query the query
     * @return the string
     */
    default String findPhone(final PasswordManagementQuery query) {
        return null;
    }

    /**
     * Find username linked to the email.
     *
     * @param query the query
     * @return the string
     */
    default String findUsername(final PasswordManagementQuery query) {
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
     */
    default Map<String, String> getSecurityQuestions(final PasswordManagementQuery query) {
        return new LinkedHashMap<>(0);
    }

    /**
     * Update security questions.
     *
     * @param query the query
     */
    default void updateSecurityQuestions(final PasswordManagementQuery query) {
    }

    /**
     * Checks a security questions answer.
     *
     * @param query    the query
     * @param question the text of the question
     * @param answer   stored answer
     * @param input    user response to question
     * @return whether the answer is correct
     */
    default boolean isValidSecurityQuestionAnswer(final PasswordManagementQuery query, final String question,
                                                  final String answer, final String input) {
        return StringUtils.isNotBlank(answer) && answer.equals(input);
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
