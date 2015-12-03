package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.NullPrincipal;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The {@link DefaultAuthenticationContext} represents a concrete
 * implementation of the authentication context. It acts as a carrier
 * to hold authentication sessions established during the processing
 * of a given request, and identifies which of those sessions
 * can be considered the primary.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public final class DefaultAuthenticationContext implements AuthenticationContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthenticationContext.class);

    private final Set<Authentication> authentications = new LinkedHashSet<>();
    private final PrincipalFactory principalFactory;
    private final AuthenticationBuilder authenticationBuilder;

    /**
     * Instantiates a new Default authentication context.
     *
     * @param authentication       the authentication
     * @param principalFactory     the principal factory
     * @param authenicationBuilder the authenication builder
     */
    public DefaultAuthenticationContext(final Authentication authentication,
                                        final PrincipalFactory principalFactory,
                                        final AuthenticationBuilder authenicationBuilder) {
        collect(authentication);
        this.principalFactory = principalFactory;
        this.authenticationBuilder = authenicationBuilder;
    }

    @Override
    public int count() {
        return this.authentications.size();
    }

    @Override
    public boolean isEmpty() {
        return !this.authentications.isEmpty()
                && getPrimaryAuthentication() != null
                && getPrimaryPrincipal() != null;
    }

    @Override
    public boolean collect(final Authentication authentication) {
        final Principal principalMismatched = validatePossibleMismatchedPrincipal(authentication);

        if (principalMismatched != null) {
            LOGGER.warn("The provided principal [{}] does not match the authentication chain. CAS has no record of "
                            + "this principal ever having authenticated in the active authentication context.",
                    authentication.getPrincipal());
            throw new IllegalArgumentException(new MixedPrincipalException(authentication,
                    authentication.getPrincipal(), principalMismatched));
        }
        return this.authentications.add(authentication);
    }

    @Override
    public Principal getPrimaryPrincipal() {
        final Authentication primary = getPrimaryAuthentication();
        if (primary == null) {
            return NullPrincipal.getInstance();
        }
        return getPrimaryAuthentication().getPrincipal();
    }

    @Override
    public Authentication getPrimaryAuthentication() {
        if (!isEmpty()) {
            /**
             * Principal id is and must be enforced to be the same for all authentication contexts.
             * Based on that restriction, it's safe to simply grab the first principal id in the chain
             * when composing the authentication chain for the caller.
             */
            final Principal primaryPrincipal = this.authentications.iterator().next().getPrincipal();


            final Map<String, Object> authenticationAttributes = new HashMap<>();
            final Map<String, Object> principalAttributes = new HashMap<>();

            for (final Authentication authn : this.authentications) {
                final Principal authenticatedPrincipal = authn.getPrincipal();
                principalAttributes.putAll(authenticatedPrincipal.getAttributes());

                for (final String attrName : authn.getAttributes().keySet()) {
                    if (!authenticationAttributes.containsKey(attrName)) {
                        authenticationAttributes.put(attrName, authn.getAttributes().get(attrName));
                    } else {
                        final Object oldValue = authenticationAttributes.remove(attrName);
                        final Collection<Object> listOfValues = convertValueToCollection(oldValue);

                        listOfValues.add(authn.getAttributes().get(attrName));
                        authenticationAttributes.put(attrName, listOfValues);
                    }
                }


                this.authenticationBuilder.set
            }

            final Principal compositePrincipal = this.principalFactory
                    .createPrincipal(primaryPrincipal.getId(), principalAttributes);

            this.authenticationBuilder.addSuccess()
            return new DefaultCompositeAuthentication(compositePrincipal, authenticationAttributes);
        }
        return null;
        if (this.authentications.isEmpty()) {
            return null;
        }
        return this.authentications.iterator().next();
    }

    /**
     * Enumerates the list of available principals in the authentication chain
     * and ensures that the newly given and provided principal is compliant
     * and equals the rest of the principals in the chain.
     * implementation.
     *
     * @param authentication the authentication object whose principal is compared against the chain
     * @return true if no mismatch is found; false otherwise.
     */
    private Principal validatePossibleMismatchedPrincipal(final Authentication authentication) {
        final Principal newPrincipal = authentication.getPrincipal();
        for (final Authentication authn : this.authentications) {
            final Principal currentPrincipal = authn.getPrincipal();

            if (!currentPrincipal.equals(newPrincipal)) {
                return currentPrincipal;
            }
        }
        return null;
    }

    /**
     * Convert the object given into a {@link Collection} instead.
     * @param obj the object to convert into a collection
     * @return The collection instance containing the object provided
     */
    @SuppressWarnings("unchecked")
    public static Set<Object> convertValueToCollection(final Object obj) {
        final Set<Object> c = new HashSet<>();

        if (obj instanceof Collection) {
            c.addAll((Collection<Object>) obj);
        } else if (obj instanceof Map) {
            throw new UnsupportedOperationException(Map.class.getCanonicalName() + " is not supported");
        } else if (obj.getClass().isArray()) {
            c.addAll(Arrays.asList((Object[]) obj));
        } else {
            c.add(obj);
        }
        return c;
    }
}
