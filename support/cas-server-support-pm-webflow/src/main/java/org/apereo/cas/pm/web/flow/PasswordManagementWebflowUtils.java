package org.apereo.cas.pm.web.flow;

import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.Map;

/**
 * This is {@link PasswordManagementWebflowUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@UtilityClass
public class PasswordManagementWebflowUtils {
    /**
     * Put password reset token.
     *
     * @param requestContext the request context
     * @param token          the token
     */
    public void putPasswordResetToken(final RequestContext requestContext, final String token) {
        val flowScope = requestContext.getFlowScope();
        flowScope.put(PasswordManagementService.PARAMETER_TOKEN, token);
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
     * Put password reset security questions.
     *
     * @param requestContext the request context
     * @param value          the value
     */
    public void putPasswordResetSecurityQuestions(final RequestContext requestContext, final Map<String, String> value) {
        val flowScope = requestContext.getFlowScope();
        flowScope.put("questions", value);
    }

    /**
     * Gets password reset questions.
     *
     * @param <T>            the type parameter
     * @param requestContext the request context
     * @param clazz          the clazz
     * @return the password reset questions
     */
    public static <T> T getPasswordResetQuestions(final RequestContext requestContext,
                                                  final Class<T> clazz) {
        val flowScope = requestContext.getFlowScope();
        return flowScope.get("questions", clazz);
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
     * @return true /false
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
     * Put password reset request.
     *
     * @param requestContext the request context
     * @param request        the request
     */
    public static void putPasswordResetRequest(final RequestContext requestContext, final PasswordResetRequest request) {
        val flowScope = requestContext.getFlowScope();
        flowScope.put(CasWebflowConstants.ATTRIBUTE_PASSWORD_MANAGEMENT_REQUEST, request);
    }

    /**
     * Gets password reset request.
     *
     * @param requestContext the request context
     * @return the password reset request
     */
    public PasswordResetRequest getPasswordResetRequest(final RequestContext requestContext) {
        val flowScope = requestContext.getFlowScope();
        return flowScope.get(CasWebflowConstants.ATTRIBUTE_PASSWORD_MANAGEMENT_REQUEST, PasswordResetRequest.class);
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
        return flowScope.getString(PasswordManagementService.PARAMETER_TOKEN);
    }
}
