package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;

import lombok.val;

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
    default boolean canImpersonate(final String surrogate, final Principal principal, final Optional<Service> service) throws Throwable {
        return false;
    }

    /**
     * Gets a collection of account names a surrogate can authenticate as.
     *
     * @param username The username of the surrogate
     * @return collection of usernames
     * @throws Throwable the throwable
     */
    Collection<String> getImpersonationAccounts(String username) throws Throwable;

    /**
     * Is wildcarded account authorized?.
     *
     * @param surrogate the surrogate
     * @param principal the principal
     * @return true /false
     * @throws Throwable the throwable
     */
    default boolean isWildcardedAccount(final String surrogate, final Principal principal) throws Throwable{
        val accounts = getImpersonationAccounts(principal.getId());
        return isWildcardedAccount(accounts);
    }

    /**
     * Is wildcarded account acepted and found in the given accounts?.
     *
     * @param accounts the accounts
     * @return true/false
     */
    default boolean isWildcardedAccount(final Collection<String> accounts) {
        return accounts.size() == 1 && accounts.contains(SurrogateAuthenticationService.WILDCARD_ACCOUNT);
    }
}
