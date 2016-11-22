package org.apereo.cas.authentication.principal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apereo.cas.authentication.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Delegates to one or more principal resolves in series to resolve a principal. The input to first configured resolver
 * is the authenticated credential; for every subsequent resolver, the input is a {@link Credential} whose ID is the
 * resolved principal ID of the previous resolver.
 * <p>
 * A common use case for this component is resolving a temporary principal ID from an X.509 credential followed by
 * a search (e.g. LDAP, database) for the final principal based on the temporary ID.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class ChainingPrincipalResolver implements PrincipalResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChainingPrincipalResolver.class);

    /**
     * Factory to create the principal type.
     **/
    private PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    /**
     * The chain of delegate resolvers that are invoked in order.
     */
    private List<PrincipalResolver> chain;

    /**
     * Sets the resolver chain. The resolvers other than the first one MUST be capable of performing resolution
     * on the basis of {@link Credential#getId()} alone;
     * {@link PersonDirectoryPrincipalResolver} notably meets that requirement.
     *
     * @param chain List of delegate resolvers that are invoked in a chain.
     */
    public void setChain(final List<PrincipalResolver> chain) {
        this.chain = chain;
    }

    /**
     * Resolves a credential by delegating to each of the configured resolvers in sequence. Note that the
     * final principal is taken from the first resolved principal in the chain, yet attributes are merged.
     *
     * @param credential Authenticated credential.
     * @param principal  Authenticated principal, if any.
     * @return The principal from the last configured resolver in the chain.
     */
    @Override
    public Principal resolve(final Credential credential, final Principal principal) {
        final List<Principal> principals = Lists.newArrayList();
        for (final PrincipalResolver resolver : chain) {
            if (resolver.supports(credential)) {
                LOGGER.debug("Invoking principal resolver {}", resolver.getClass().getSimpleName());
                principals.add(resolver.resolve(credential, principal));
            }
        }
        if (principals.isEmpty()) {
            LOGGER.warn("None of the principal resolvers in the chain were able to produce a principal");
            return NullPrincipal.getInstance();
        }
        final Map<String, Object> attributes = Maps.newHashMap();
        principals.forEach(p -> {
            LOGGER.debug("Adding attributes {} for the final principal", p.getAttributes());
            attributes.putAll(p.getAttributes());
        });
        final Principal finalPrincipal = this.principalFactory.createPrincipal(principals.get(0).getId(),
                attributes);
        LOGGER.debug("Final principal constructed is {}", finalPrincipal);
        return finalPrincipal;
    }

    /**
     * Determines whether the credential is supported by this component by delegating to the first configured
     * resolver in the chain.
     *
     * @param credential The credential to check for support.
     * @return True if the first configured resolver in the chain supports the credential, false otherwise.
     */
    @Override
    public boolean supports(final Credential credential) {
        return this.chain.get(0).supports(credential);
    }
}
