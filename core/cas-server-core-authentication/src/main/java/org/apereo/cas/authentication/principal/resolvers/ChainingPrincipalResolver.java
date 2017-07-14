package org.apereo.cas.authentication.principal.resolvers;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.MergingPersonAttributeDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final PrincipalFactory principalFactory = new DefaultPrincipalFactory();

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

    public void setChain(final PrincipalResolver... chain) {
        this.chain = Arrays.stream(chain).collect(Collectors.toList());
    }
    
    /**
     * {@inheritDoc}
     * Resolves a credential by delegating to each of the configured resolvers in sequence. Note that the
     * final principal is taken from the first resolved principal in the chain, yet attributes are merged.
     *
     * @param credential Authenticated credential.
     * @param principal  Authenticated principal, if any.
     * @return The principal from the last configured resolver in the chain.
     */
    @Override
    public Principal resolve(final Credential credential, final Principal principal, final AuthenticationHandler handler) {
        final List<Principal> principals = new ArrayList<>();
        chain.stream()
                .filter(resolver -> resolver.supports(credential))
                .forEach(resolver -> {
                    LOGGER.debug("Invoking principal resolver [{}]", resolver);
                    final Principal p = resolver.resolve(credential, principal, handler);
                    if (p != null) {
                        principals.add(p);
                    }
                });

        if (principals.isEmpty()) {
            LOGGER.warn("None of the principal resolvers in the chain were able to produce a principal");
            return NullPrincipal.getInstance();
        }

        final Map<String, Object> attributes = new HashMap<>();
        principals.forEach(p -> {
            if (p != null) {
                LOGGER.debug("Resolved principal [{}]", p);
                if (p.getAttributes() != null && !p.getAttributes().isEmpty()) {
                    LOGGER.debug("Adding attributes [{}] for the final principal", p.getAttributes());
                    attributes.putAll(p.getAttributes());
                }
            }
        });

        final long count = principals.stream()
                .map(p -> p.getId().trim().toLowerCase())
                .distinct()
                .collect(Collectors.toSet()).size();

        if (count > 1) {
            throw new PrincipalException("Resolved principals by the chain are not unique because principal resolvers have produced CAS principals "
                    + "with different identifiers which typically is the result of a configuration issue.",
                    new HashMap<>(0),
                    new HashMap<>(0));
        }
        final String principalId = principal != null ? principal.getId() : principals.get(0).getId();
        final Principal finalPrincipal = this.principalFactory.createPrincipal(principalId, attributes);
        LOGGER.debug("Final principal constructed by the chain of resolvers is [{}]", finalPrincipal);
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("chain", chain)
                .toString();
    }

    @Override
    public IPersonAttributeDao getAttributeRepository() {
        final MergingPersonAttributeDaoImpl dao = new MergingPersonAttributeDaoImpl();
        dao.setPersonAttributeDaos(this.chain.stream().map(PrincipalResolver::getAttributeRepository).collect(Collectors.toList()));
        return dao;
    }
}
