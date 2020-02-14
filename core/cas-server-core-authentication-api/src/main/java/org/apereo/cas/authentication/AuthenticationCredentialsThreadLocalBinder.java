package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;

import lombok.NoArgsConstructor;
import lombok.val;

import java.util.Arrays;
import java.util.Collection;

/**
 * ThreadLocal based holder for current set of credentials and/or authentication object for any current
 * CAS authentication transaction. Useful for making this information available to all the interested CAS
 * components that are not tightly coupled with core CAS APIs, for example audit principal resolver component, etc.
 * <p>
 * The thread local state carried by this class should be set by core CAS components processing core authentication and
 * CAS protocol events e.g. {@code AuthenticationManager}, {@code CentralAuthenticationService}, etc.
 * <p>
 * The clearing of this state at the end of a thread execution path is the responsibility
 * of {@code AuthenticationCredentialsLocalBinderClearingFilter}
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@NoArgsConstructor
public class AuthenticationCredentialsThreadLocalBinder {

    private static final ThreadLocal<Authentication> CURRENT_AUTHENTICATION = new ThreadLocal<>();

    private static final ThreadLocal<Principal> IN_PROGRESS_PRINCIPAL = new ThreadLocal<>();

    private static final ThreadLocal<Authentication> IN_PROGRESS_AUTHENTICATION = new ThreadLocal<>();

    private static final ThreadLocal<AuthenticationBuilder> CURRENT_AUTHENTICATION_BUILDER = new ThreadLocal<>();

    private static final ThreadLocal<String[]> CURRENT_CREDENTIAL_IDS = new ThreadLocal<>();

    /**
     * Bind Principal to ThreadLocal for principal that has been authenticated but for which attributes have not yet been retrieved.
     * This can make attributes from the authentication event (e.g. X509, LDAP, WSFED) available to scripted attribute repositories.
     * If a principal is already bound, combine the attributes with new attributes overriding existing.
     * CAS won't use this as the principal, this principal is just an attribute bucket that will start with authentication
     * attributes and collect more attributes as attribute repositories are queried.
     * @param principal the "in progress" principal
     */
    public static void bindInProgressPrincipal(final Principal principal) {
        val inProgressPrincipal = IN_PROGRESS_PRINCIPAL.get();
        if (inProgressPrincipal != null) {
            inProgressPrincipal.getAttributes().putAll(principal.getAttributes());
        } else {
            IN_PROGRESS_PRINCIPAL.set(principal);
        }
    }

    /**
     * Bind Authentication to ThreadLocal for authentication event that has internally being processed and yet hasn't been fully established
     * or selected by CAS to resume in later parts of the authentication flow, etc. This is typically used by the authentication manager to remember
     * the current authentication object that is in the process of execution, carrying it to the next handler, etc if needed.
     *
     * @param authentication the authentication
     */
    public static void bindInProgress(final Authentication authentication) {
        IN_PROGRESS_AUTHENTICATION.set(authentication);
    }

    /**
     * Bind credentials to ThreadLocal.
     *
     * @param credentials the credentials
     */
    public static void bindCurrent(final Collection<Credential> credentials) {
        bindCurrent(credentials.toArray(Credential[]::new));
    }

    /**
     * Bind credentials to ThreadLocal.
     *
     * @param credentials the credentials
     */
    public static void bindCurrent(final Credential... credentials) {
        CURRENT_CREDENTIAL_IDS.set(Arrays.stream(credentials).map(Credential::getId).toArray(String[]::new));
    }

    /**
     * Bind Authentication to ThreadLocal.
     *
     * @param authentication the authentication
     */
    public static void bindCurrent(final Authentication authentication) {
        CURRENT_AUTHENTICATION.set(authentication);
    }

    /**
     * Bind AuthenticationBuilder to ThreadLocal.
     *
     * @param builder the authentication builder
     */
    public static void bindCurrent(final AuthenticationBuilder builder) {
        CURRENT_AUTHENTICATION_BUILDER.set(builder);
    }

    /**
     * Get credential ids from ThreadLocal.
     *
     * @return credential ids
     */
    public static String[] getCurrentCredentialIds() {
        return CURRENT_CREDENTIAL_IDS.get();
    }

    /**
     * Get credential ids String representation from ThreadLocal.
     *
     * @return credential ids String representation
     */
    public static String getCurrentCredentialIdsAsString() {
        return getCurrentCredentialIds() != null ? String.join(", ", getCurrentCredentialIds()) : null;
    }

    /**
     * Get AuthenticationBuilder from ThreadLocal.
     *
     * @return authentication builder
     */
    public static AuthenticationBuilder getCurrentAuthenticationBuilder() {
        return CURRENT_AUTHENTICATION_BUILDER.get();
    }

    /**
     * Get Authentication from ThreadLocal.
     *
     * @return authentication
     */
    public static Authentication getCurrentAuthentication() {
        return CURRENT_AUTHENTICATION.get();
    }

    /**
     * Get Authentication from ThreadLocal.
     *
     * @return authentication
     */
    public static Authentication getInProgressAuthentication() {
        return IN_PROGRESS_AUTHENTICATION.get();
    }

    /**
     * Get Principal from ThreadLocal.
     *
     * @return principal
     */
    public static Principal getInProgressPrincipal() {
        return IN_PROGRESS_PRINCIPAL.get();
    }


    /**
     * Clear ThreadLocal state of IN_PROGRESS_AUTHENTICATION.
     */
    public static void clearInProgressAuthentication() {
        IN_PROGRESS_AUTHENTICATION.remove();
    }

    /**
     * Clear ThreadLocal state of IN_PROGRESS_PRINCIPAL.
     */
    public static void clearInProgressPrincipal() {
        IN_PROGRESS_PRINCIPAL.remove();
    }

    /**
     * Clear ThreadLocal state.
     */
    public static void clear() {
        CURRENT_CREDENTIAL_IDS.remove();
        CURRENT_AUTHENTICATION.remove();
        CURRENT_AUTHENTICATION_BUILDER.remove();
        clearInProgressPrincipal();
        clearInProgressAuthentication();
    }
}
