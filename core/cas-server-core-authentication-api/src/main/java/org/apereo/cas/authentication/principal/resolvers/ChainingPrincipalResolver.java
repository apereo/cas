package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.MergingPersonAttributeDaoImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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
@Slf4j
@ToString
@Setter
@RequiredArgsConstructor
public class ChainingPrincipalResolver implements PrincipalResolver {

    /**
     * Factory to create the principal type.
     **/
    private final PrincipalFactory principalFactory = PrincipalFactoryUtils.newPrincipalFactory();

    /**
     * The chain of delegate resolvers that are invoked in order.
     */
    private List<PrincipalResolver> chain;

    private final PrincipalElectionStrategy principalElectionStrategy;
    
    @Override
    public Principal resolve(final Credential credential, final Optional<Principal> principal, final Optional<AuthenticationHandler> handler) {
        val principals = new ArrayList<Principal>(chain.size());
        chain.stream()
            .filter(resolver -> resolver.supports(credential))
            .forEach(resolver -> {
                LOGGER.debug("Invoking principal resolver [{}]", resolver.getName());
                val p = resolver.resolve(credential, principal, handler);
                if (p != null) {
                    LOGGER.debug("Resolved principal [{}]", p);
                    principals.add(p);
                }
            });
        if (principals.isEmpty()) {
            LOGGER.warn("None of the principal resolvers in the chain were able to produce a principal");
            return NullPrincipal.getInstance();
        }
        val attributes = new HashMap<String, List<Object>>();
        principals.forEach(p -> {
            if (p != null) {
                LOGGER.debug("Resolved principal [{}]", p);
                val principalAttributes = p.getAttributes();
                if (principalAttributes != null && !principalAttributes.isEmpty()) {
                    LOGGER.debug("Adding attributes [{}] for the final principal", principalAttributes);
                    attributes.putAll(CoreAuthenticationUtils.mergeAttributes(attributes, principalAttributes));
                }
            }
        });
        return principalElectionStrategy.nominate(principals, attributes);
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
        return this.chain.stream().anyMatch(r -> r.supports(credential));
    }

    @Override
    public IPersonAttributeDao getAttributeRepository() {
        val dao = new MergingPersonAttributeDaoImpl();
        dao.setPersonAttributeDaos(this.chain.stream().map(PrincipalResolver::getAttributeRepository).collect(Collectors.toList()));
        return dao;
    }
}
