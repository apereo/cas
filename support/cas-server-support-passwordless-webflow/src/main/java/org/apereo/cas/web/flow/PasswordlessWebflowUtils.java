package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PasswordlessWebflowUtils}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@UtilityClass
public class PasswordlessWebflowUtils {

    /**
     * List of attributes that should be mapped onto subflows
     * from the passwordless flow.
     */
    public static final List<String> WEBFLOW_ATTRIBUTE_MAPPINGS = List.of("passwordlessAccount", "passwordlessAuthenticationRequest");

    /**
     * Put passwordless authentication enabled.
     *
     * @param requestContext the request context
     * @param value          the value
     */
    public static void putPasswordlessAuthenticationEnabled(final RequestContext requestContext, final Boolean value) {
        requestContext.getFlowScope().put("passwordlessAuthenticationEnabled", value);
    }

    /**
     * Is passwordless authentication enabled.
     *
     * @param requestContext the request context
     * @return true/false
     */
    public static boolean isPasswordlessAuthenticationEnabled(final RequestContext requestContext) {
        return requestContext.getFlowScope().getBoolean("passwordlessAuthenticationEnabled", Boolean.FALSE);
    }
    
    /**
     * Put passwordless authentication request.
     *
     * @param requestContext      the request context
     * @param passwordlessRequest the passwordless request
     */
    public static void putPasswordlessAuthenticationRequest(final RequestContext requestContext, final Serializable passwordlessRequest) {
        requestContext.getFlowScope().put("passwordlessAuthenticationRequest", passwordlessRequest);
    }

    /**
     * Gets passwordless authentication request.
     *
     * @param <T>            the type parameter
     * @param requestContext the request context
     * @param clazz          the clazz
     * @return the passwordless authentication request
     */
    public static <T> T getPasswordlessAuthenticationRequest(final RequestContext requestContext, final Class<T> clazz) {
        return requestContext.getFlowScope().get("passwordlessAuthenticationRequest", clazz);
    }

    /**
     * Put passwordless authentication account.
     *
     * @param requestContext the request context
     * @param account        the account
     */
    public static void putPasswordlessAuthenticationAccount(final RequestContext requestContext, final Object account) {
        requestContext.getFlowScope().put("passwordlessAccount", account);
    }

    /**
     * Gets passwordless authentication account.
     *
     * @param <T>   the type parameter
     * @param event the event
     * @param clazz the clazz
     * @return the passwordless authentication account
     */
    public static @Nullable <T> T getPasswordlessAuthenticationAccount(final Event event, final Class<T> clazz) {
        if (event != null) {
            return event.getAttributes().get("passwordlessAccount", clazz);
        }
        return null;
    }

    /**
     * Gets passwordless authentication account.
     *
     * @param <T>            the type parameter
     * @param requestContext the context
     * @param clazz          the clazz
     * @return the passwordless authentication account
     */
    public static @Nullable <T> T getPasswordlessAuthenticationAccount(final RequestContext requestContext, final Class<T> clazz) {
        var result = getPasswordlessAuthenticationAccount(requestContext.getCurrentEvent(), clazz);
        if (result == null) {
            result = requestContext.getFlowScope().get("passwordlessAccount", clazz);
        }
        return result;
    }

    /**
     * Has passwordless authentication account.
     *
     * @param requestContext the request context
     * @return true /false
     */
    public static boolean hasPasswordlessAuthenticationAccount(final RequestContext requestContext) {
        return requestContext.getFlowScope().contains("passwordlessAccount");
    }

    /**
     * Put passwordless captcha enabled.
     *
     * @param requestContext the request context
     * @param recaptcha      the recaptcha
     */
    public static void putPasswordlessCaptchaEnabled(final RequestContext requestContext, final GoogleRecaptchaProperties recaptcha) {
        requestContext.getFlowScope().put("passwordlessCaptchaEnabled", recaptcha.isEnabled());
    }

    /**
     * Is passwordless captcha enabled.
     *
     * @param requestContext the request context
     * @return true/false
     */
    public static boolean isPasswordlessCaptchaEnabled(final RequestContext requestContext) {
        val enabled = Objects.requireNonNullElse(requestContext.getFlowScope().getBoolean("passwordlessCaptchaEnabled", Boolean.FALSE), Boolean.FALSE);
        return BooleanUtils.toBoolean(enabled);
    }

    /**
     * Put multifactor authentication allowed.
     *
     * @param requestContext the request context
     * @param value          the value
     */
    public static void putMultifactorAuthenticationAllowed(final RequestContext requestContext, final boolean value) {
        requestContext.getFlowScope().put("passwordlessMultifactorAuthenticationAllowed", value);
    }

    /**
     * Put delegated authentication allowed.
     *
     * @param requestContext the request context
     * @param value          the value
     */
    public static void putDelegatedAuthenticationAllowed(final RequestContext requestContext, final boolean value) {
        requestContext.getFlowScope().put("passwordlessDelegatedAuthenticationAllowed", value);
    }

    /**
     * Is multifactor authentication allowed.
     *
     * @param context the context
     * @return true or false
     */
    public static boolean isMultifactorAuthenticationAllowed(final RequestContext context) {
        return context.getFlowScope().getBoolean("passwordlessMultifactorAuthenticationAllowed", Boolean.FALSE);
    }

    /**
     * Is delegated authentication allowed.
     *
     * @param context the context
     * @return true or false
     */
    public static boolean isDelegatedAuthenticationAllowed(final RequestContext context) {
        return context.getFlowScope().getBoolean("passwordlessDelegatedAuthenticationAllowed", Boolean.FALSE);
    }
}
