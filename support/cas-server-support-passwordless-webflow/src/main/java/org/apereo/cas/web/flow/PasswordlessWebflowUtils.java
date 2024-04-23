package org.apereo.cas.web.flow;

import lombok.experimental.UtilityClass;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.List;

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
    public static <T> T getPasswordlessAuthenticationAccount(final Event event, final Class<T> clazz) {
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
    public static <T> T getPasswordlessAuthenticationAccount(final RequestContext requestContext, final Class<T> clazz) {
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

}
