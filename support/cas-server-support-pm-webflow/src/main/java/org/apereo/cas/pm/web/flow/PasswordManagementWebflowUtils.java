package org.apereo.cas.pm.web.flow;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;

/**
 * This is {@link PasswordManagementWebflowUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@UtilityClass
public class PasswordManagementWebflowUtils {
    /**
     * Param name for the token.
     */
    public static final String REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN = "pswdrst";

    /**
     * Flowscope param name for token.
     */
    public static final String FLOWSCOPE_PARAMETER_NAME_TOKEN = "token";

    /**
     * Put password reset token.
     *
     * @param requestContext the request context
     * @param token          the token
     */
    public void putPasswordResetToken(final RequestContext requestContext, final String token) {
        val flowScope = requestContext.getFlowScope();
        flowScope.put(FLOWSCOPE_PARAMETER_NAME_TOKEN, token);
    }

    /**
     * Put password reset security questions.
     *
     * @param requestContext the request context
     * @param value          the value
     */
    public void putPasswordResetSecurityQuestions(final RequestContext requestContext, final List<String> value) {
        val flowScope = requestContext.getFlowScope();
        flowScope.put("questions", value);
    }

    /**
     * Gets password reset questions.
     *
     * @param requestContext the request context
     * @return the password reset questions
     */
    public static List<String> getPasswordResetQuestions(final RequestContext requestContext) {
        val flowScope = requestContext.getFlowScope();
        return flowScope.get("questions", List.class);
    }

    /**
     * Put password reset security questions enabled.
     *
     * @param requestContext the request context
     * @param value          the value
     */
    public static void putPasswordResetSecurityQuestionsEnabled(final RequestContext requestContext, final boolean value) {
        val flowScope = requestContext.getFlowScope();
        flowScope.put("questionsEnabled", value);
    }

    /**
     * Is password reset security questions enabled boolean.
     *
     * @param requestContext the request context
     * @return true/false
     */
    public static boolean isPasswordResetSecurityQuestionsEnabled(final RequestContext requestContext) {
        val flowScope = requestContext.getFlowScope();
        return flowScope.getBoolean("questionsEnabled");
    }

    /**
     * Put password reset username.
     *
     * @param requestContext the request context
     * @param username       the username
     */
    public static void putPasswordResetUsername(final RequestContext requestContext, final String username) {
        val flowScope = requestContext.getFlowScope();
        flowScope.put("username", username);
    }

    /**
     * Gets password reset username.
     *
     * @param requestContext the request context
     * @return the password reset username
     */
    public static String getPasswordResetUsername(final RequestContext requestContext) {
        val flowScope = requestContext.getFlowScope();
        return flowScope.getString("username");
    }

    /**
     * Gets password reset token.
     *
     * @param requestContext the request context
     * @return the password reset token
     */
    public static String getPasswordResetToken(final RequestContext requestContext) {
        val flowScope = requestContext.getFlowScope();
        return flowScope.getString(FLOWSCOPE_PARAMETER_NAME_TOKEN);
    }

    /**
     * Put password reset password policy pattern string.
     *
     * @param requestContext the request context
     * @param policyPattern  the policy pattern
     */
    public static void putPasswordResetPasswordPolicyPattern(final RequestContext requestContext, final String policyPattern) {
        val flowScope = requestContext.getFlowScope();
        flowScope.put("policyPattern", policyPattern);
    }
}
