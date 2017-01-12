package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * This is {@link DefaultPrincipalElectionStrategy} that selects the primary principal
 * to be the first principal in the chain of authentication history.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class DefaultPrincipalElectionStrategy implements PrincipalElectionStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPrincipalElectionStrategy.class);
    private static final long serialVersionUID = 6704726217030836315L;
    
    private final PrincipalFactory principalFactory;

    public DefaultPrincipalElectionStrategy() {
        this(new DefaultPrincipalFactory());
    }

    public DefaultPrincipalElectionStrategy(final PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }

    @Override
    public Principal nominate(final Collection<Authentication> authentications, 
                              final Map<String, Object> principalAttributes) {
        final Principal principal = authentications.iterator().next().getPrincipal();
        final Principal finalPrincipal = this.principalFactory.createPrincipal(principal.getId(), principalAttributes);
        LOGGER.debug("Nominated [{}] as the primary principal", finalPrincipal);
        return finalPrincipal;
    }
}
