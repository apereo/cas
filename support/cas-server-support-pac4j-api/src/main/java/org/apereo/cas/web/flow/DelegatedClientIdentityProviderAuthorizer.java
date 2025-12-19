package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.Client;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link DelegatedClientIdentityProviderAuthorizer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public interface DelegatedClientIdentityProviderAuthorizer {

    /**
     * The bean name.
     */
    String BEAN_NAME = "delegatedClientIdentityProviderAuthorizer";

    /**
     * Is delegated client authorized for service?
     *
     * @param client  the client
     * @param service the service
     * @param context the context
     * @return true /false
     * @throws Throwable the throwable
     */
    default boolean isDelegatedClientAuthorizedForService(final Client client, final Service service,
                                                          final HttpServletRequest context) throws Throwable {
        return isDelegatedClientAuthorizedFor(client.getName(), service, context);
    }

    /**
     * Is delegated client authorized for service?
     *
     * @param client  the client
     * @param service the service
     * @param context the context
     * @return true /false
     * @throws Throwable the throwable
     */
    default boolean isDelegatedClientAuthorizedForService(final Client client, final Service service,
                                                          final RequestContext context) throws Throwable {
        return isDelegatedClientAuthorizedFor(client.getName(), service, context);
    }

    /**
     * Is delegated client authorized for authentication?
     *
     * @param authentication the authentication
     * @param service        the service
     * @param context        the context
     * @return true /false
     * @throws Throwable the throwable
     */
    default boolean isDelegatedClientAuthorizedForAuthentication(final Authentication authentication,
                                                                 final Service service,
                                                                 final RequestContext context) throws Throwable {
        val clientName = getClientNameFromAuthentication(authentication);
        return isDelegatedClientAuthorizedFor(clientName, service, context);
    }

    /**
     * Gets client name from authentication.
     *
     * @param authentication the authentication
     * @return the client name from authentication
     */
    default String getClientNameFromAuthentication(final Authentication authentication) {
        val clientNames = authentication.getAttributes().getOrDefault(
            ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME, new ArrayList<>());
        return CollectionUtils.firstElement(clientNames).map(Object::toString).orElse(StringUtils.EMPTY);
    }

    /**
     * Is delegated client authorized for.
     *
     * @param clientName the client name
     * @param service    the service
     * @param context    the context
     * @return true /false
     * @throws Throwable the throwable
     */
    boolean isDelegatedClientAuthorizedFor(String clientName, Service service,
                                           RequestContext context) throws Throwable;

    /**
     * Is delegated client authorized for.
     *
     * @param clientName the client name
     * @param service    the service
     * @param request    the request
     * @return true /false
     * @throws Throwable the throwable
     */
    boolean isDelegatedClientAuthorizedFor(String clientName, Service service,
                                           HttpServletRequest request) throws Throwable;
}
