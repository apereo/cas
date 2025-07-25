package org.apereo.cas.web.flow;

import org.apereo.cas.ticket.Ticket;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.webflow.execution.RequestContext;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is {@link DelegationWebflowUtils}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@UtilityClass
public class DelegationWebflowUtils {

    /**
     * Flow scope attribute to indicate the primary chosen identity provider.
     */
    public static final String FLOW_SCOPE_ATTR_DELEGATED_AUTHN_PROVIDER_PRIMARY = "delegatedAuthenticationProviderPrimary";

    /**
     * Gets delegated client authentication candidate profile.
     *
     * @param <T>     the type parameter
     * @param context the context
     * @param clazz   the clazz
     * @return the delegated client authentication candidate profile
     */
    public static <T> T getDelegatedClientAuthenticationCandidateProfile(final RequestContext context,
                                                                         final Class<T> clazz) {
        return context.getFlashScope().get("delegatedClientAuthenticationCandidateProfile", clazz);
    }

    /**
     * Has delegated client authentication candidate profile?
     *
     * @param context the context
     * @return true /false
     */
    public static boolean hasDelegatedClientAuthenticationCandidateProfile(final RequestContext context) {
        return context.getFlashScope().contains("delegatedClientAuthenticationCandidateProfile");
    }

    /**
     * Put delegated client authentication candidate profile.
     *
     * @param context the context
     * @param profile the profile
     */
    public static void putDelegatedClientAuthenticationCandidateProfile(final RequestContext context,
                                                                        final Serializable profile) {
        context.getFlashScope().put("delegatedClientAuthenticationCandidateProfile", profile);
    }

    /**
     * Put delegated authentication logout request.
     *
     * @param requestContext the request context
     * @param logoutAction   the logout action
     */
    public static void putDelegatedAuthenticationLogoutRequest(final RequestContext requestContext,
                                                               final Serializable logoutAction) {
        requestContext.getFlashScope().put("delegatedAuthenticationLogoutRequest", logoutAction);
    }

    /**
     * Gets delegated authentication logout request.
     *
     * @param <T>            the type parameter
     * @param requestContext the request context
     * @param clazz          the clazz
     * @return the delegated authentication logout request
     */
    public <T> T getDelegatedAuthenticationLogoutRequest(final RequestContext requestContext,
                                                         final Class<T> clazz) {
        return requestContext.getFlashScope().get("delegatedAuthenticationLogoutRequest", clazz);
    }

    /**
     * Put delegated authentication.
     *
     * @param requestContext the request context
     * @param disabled       the disabled
     */
    public static void putDelegatedAuthenticationDisabled(final RequestContext requestContext, final boolean disabled) {
        requestContext.getFlowScope().put("delegatedAuthenticationDisabled", disabled);
    }

    /**
     * Put delegated client authentication resolved credentials.
     *
     * @param context          the context
     * @param candidateMatches the candidate matches
     */
    public static void putDelegatedClientAuthenticationResolvedCredentials(final RequestContext context,
                                                                           final List<? extends Serializable> candidateMatches) {
        context.getFlowScope().put("delegatedAuthenticationCredentials", candidateMatches);
    }

    /**
     * Gets delegated client authentication resolved credentials.
     *
     * @param <T>     the type parameter
     * @param context the context
     * @param clazz   the clazz
     * @return the delegated client authentication resolved credentials
     */
    public static <T extends Serializable> List<T> getDelegatedClientAuthenticationResolvedCredentials(final RequestContext context,
                                                                                                       final Class<T> clazz) {
        val results = context.getFlowScope().get("delegatedAuthenticationCredentials", List.class);
        return ObjectUtils.getIfNull(results, List.of());
    }


    /**
     * Gets delegated authentication client name.
     *
     * @param requestContext the request context
     * @return the delegated authentication client name
     */
    public static String getDelegatedAuthenticationClientName(final RequestContext requestContext) {
        return requestContext.getFlowScope().get("delegatedAuthenticationClientName", String.class);
    }

    /**
     * Put delegated authentication client name.
     *
     * @param requestContext the request context
     * @param clientName     the client name
     */
    public static void putDelegatedAuthenticationClientName(final RequestContext requestContext, final String clientName) {
        requestContext.getFlowScope().put("delegatedAuthenticationClientName", clientName);
    }

    /**
     * Gets delegated authentication provider configurations.
     *
     * @param context the context
     * @return the delegated authentication provider configurations
     */
    public static Set<? extends Serializable> getDelegatedAuthenticationProviderConfigurations(final RequestContext context) {
        val scope = context.getFlowScope();
        if (scope.contains("delegatedAuthenticationProviderConfigurations", Set.class)) {
            return scope.get("delegatedAuthenticationProviderConfigurations", Set.class);
        }
        return new HashSet<>();
    }

    /**
     * Put delegated authentication provider configurations.
     *
     * @param context the context
     * @param urls    the urls
     */
    public static void putDelegatedAuthenticationProviderConfigurations(final RequestContext context,
                                                                        final Set<? extends Serializable> urls) {
        context.getFlowScope().put("delegatedAuthenticationProviderConfigurations", urls);
    }

    /**
     * Put delegated authentication dynamic provider selection.
     *
     * @param context the context
     * @param result  the result
     */
    public static void putDelegatedAuthenticationDynamicProviderSelection(final RequestContext context,
                                                                          final Boolean result) {
        context.getFlowScope().put("delegatedAuthenticationDynamicProviderSelection", result);
    }

    /**
     * Put delegated authentication dynamic provider selection.
     *
     * @param context the context
     * @return true/false
     */
    public static Boolean isDelegatedAuthenticationDynamicProviderSelection(final RequestContext context) {
        return context.getFlowScope().get("delegatedAuthenticationDynamicProviderSelection", Boolean.class, Boolean.FALSE);
    }

    /**
     * Put delegated authentication provider dominant.
     *
     * @param context the context
     * @param client  the client
     */
    public static void putDelegatedAuthenticationProviderPrimary(final RequestContext context, final Serializable client) {
        context.getFlowScope().put(FLOW_SCOPE_ATTR_DELEGATED_AUTHN_PROVIDER_PRIMARY, client);
    }

    /**
     * Gets delegated authentication provider primary.
     *
     * @param context the context
     * @return the delegated authentication provider primary
     */
    public static Object getDelegatedAuthenticationProviderPrimary(final RequestContext context) {
        return context.getFlowScope().get(FLOW_SCOPE_ATTR_DELEGATED_AUTHN_PROVIDER_PRIMARY);
    }

    /**
     * Put delegated authentication logout request ticket.
     *
     * @param requestContext         the request context
     * @param ticket the transient session ticket
     */
    public static void putDelegatedAuthenticationLogoutRequestTicket(final RequestContext requestContext,
                                                                     final Ticket ticket) {
        if (ticket == null) {
            requestContext.getFlowScope().remove(FLOW_SCOPE_ATTR_DELEGATED_AUTHN_PROVIDER_PRIMARY);
        } else {
            requestContext.getFlowScope().put(FLOW_SCOPE_ATTR_DELEGATED_AUTHN_PROVIDER_PRIMARY, ticket);
        }
    }

    /**
     * Gets delegated authentication logout request ticket.
     *
     * @param requestContext the request context
     * @return the delegated authentication logout request ticket
     */
    public static Ticket getDelegatedAuthenticationLogoutRequestTicket(final RequestContext requestContext) {
        return requestContext.getFlowScope().get(FLOW_SCOPE_ATTR_DELEGATED_AUTHN_PROVIDER_PRIMARY, Ticket.class);
    }
}
