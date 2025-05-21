package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;

import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;

/**
 * This is {@link SurrogateAuthenticationService}.
 * It defines operations to note whether one can substitute as another during authentication.
 *
 * @author Jonathan Johnson
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface SurrogateAuthenticationService {
    /**
     * Logger instance.
     */
    Logger LOGGER = LoggerFactory.getLogger(SurrogateAuthenticationService.class);
    
    /**
     * An authorized account may be tagged as a wildcard, meaning
     * that the account has special permissions to impersonate anyone.
     */
    String WILDCARD_ACCOUNT = "*";

    /**
     * Default bean name.
     */
    String BEAN_NAME = "surrogateAuthenticationService";

    /**
     * Surrogate username attribute in the authentication payload.
     */
    String AUTHENTICATION_ATTR_SURROGATE_USER = "surrogateUser";
    
    /**
     * Original credential attribute in the authentication payload.
     */
    String AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL = "surrogatePrincipal";
    /**
     * Indicates that surrogate authn is enabled and activated.
     */
    String AUTHENTICATION_ATTR_SURROGATE_ENABLED = "surrogateEnabled";

    /**
     * Checks whether a principal can authenticate as a surrogate user.
     *
     * @param surrogate The username of the surrogate
     * @param principal the principal
     * @param service   the service
     * @return true if the given surrogate can authenticate as the user
     * @throws Throwable the throwable
     */
    default boolean canImpersonate(final String surrogate, final Principal principal, final Optional<? extends Service> service) throws Throwable {
        return false;
    }

    /**
     * Gets a collection of account names a surrogate can authenticate as.
     *
     * @param username The username of the surrogate
     * @param service  the service
     * @return collection of usernames
     * @throws Throwable the throwable
     */
    Collection<String> getImpersonationAccounts(String username, Optional<? extends Service> service) throws Throwable;

    /**
     * Is wildcarded account authorized?.
     *
     * @param surrogate the surrogate
     * @param principal the principal
     * @param service   the service
     * @return true /false
     * @throws Throwable the throwable
     */
    default boolean isWildcardedAccount(final String surrogate, final Principal principal, final Optional<? extends Service> service) throws Throwable{
        val accounts = getImpersonationAccounts(principal.getId(), service);
        return isWildcardedAccount(accounts, service);
    }

    /**
     * Is wildcarded account accepted and found in the given accounts?.
     *
     * @param accounts the accounts
     * @param service  the service
     * @return true /false
     */
    default boolean isWildcardedAccount(final Collection<String> accounts, final Optional<? extends Service> service) {
        return accounts.size() == 1 && accounts.contains(SurrogateAuthenticationService.WILDCARD_ACCOUNT);
    }

    /**
     * Collect surrogate attributes.
     *
     * @param builder       the builder
     * @param surrogateUser the surrogate user
     * @param principal     the principal
     */
    default void collectSurrogateAttributes(final AuthenticationBuilder builder,
                                            final String surrogateUser,
                                            final String principal) {
        LOGGER.debug("Recording surrogate username [{}] as an authentication attribute", surrogateUser);
        builder.addAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER, surrogateUser);
        builder.addAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL, principal);
        builder.addAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, Boolean.TRUE);
    }

}
