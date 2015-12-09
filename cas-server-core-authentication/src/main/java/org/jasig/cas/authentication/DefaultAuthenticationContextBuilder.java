package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link DefaultAuthenticationContextBuilder}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("defaultAuthenticationContextBuilder")
public class DefaultAuthenticationContextBuilder implements AuthenticationContextBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthenticationContext.class);

    private final Set<Authentication> authentications = new LinkedHashSet<>();

    @Autowired
    @Qualifier("principalFactory")
    private PrincipalFactory principalFactory;

    @Override
    public int count() {
        return this.authentications.size();
    }

    @Override
    public boolean isEmpty() {
        return !this.authentications.isEmpty();
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
    public AuthenticationContext build() {
        final Authentication authentication = buildAuthentication();
        if (authentication == null) {
            throw new RuntimeException(new GeneralSecurityException("Authentication context cannot be produced"));
        }
        return new DefaultAuthenticationContext(authentication);
    }

    @Override
    public void clear() {
        this.authentications.clear();
    }


    /**
     * Principal id is and must be enforced to be the same for all authentication contexts.
     * Based on that restriction, it's safe to simply grab the first principal id in the chain
     * when composing the authentication chain for the caller.
     */
    private Authentication buildAuthentication() {
        if (!isEmpty()) {

            final Principal primaryPrincipal = this.authentications.iterator().next().getPrincipal();
            final Map<String, Object> authenticationAttributes = new HashMap<>();
            final Map<String, Object> principalAttributes = new HashMap<>();

            final AuthenticationBuilder authenticationBuilder = DefaultAuthenticationBuilder.newInstance();

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
                authenticationBuilder.setSuccesses(authn.getSuccesses())
                                     .setFailures(authn.getFailures());
            }

            final Principal compositePrincipal = this.principalFactory
                    .createPrincipal(primaryPrincipal.getId(), principalAttributes);

            authenticationBuilder.setPrincipal(compositePrincipal);
            authenticationBuilder.setAttributes(authenticationAttributes);
            authenticationBuilder.setAuthenticationDate(DateTime.now());
            return authenticationBuilder.build();
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

}
