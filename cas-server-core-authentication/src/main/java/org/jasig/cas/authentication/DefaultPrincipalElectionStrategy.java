package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

/**
 * This is {@link DefaultPrincipalElectionStrategy} that selects the primary principal
 * to be the first principal in the chain of authentication history.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("defaultPrincipalElectionStrategy")
public final class DefaultPrincipalElectionStrategy implements PrincipalElectionStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPrincipalElectionStrategy.class);
    private static final long serialVersionUID = 6704726217030836315L;

    @Autowired
    @Qualifier("principalFactory")
    private PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    @Override
    public Principal nominate(final Collection<Authentication> authentications, final Map<String, Object> principalAttributes) {
        final Principal principal = authentications.iterator().next().getPrincipal();
        final Principal finalPrincipal = this.principalFactory.createPrincipal(principal.getId(), principalAttributes);
        LOGGER.debug("Nominated [{}] as the primary principal", finalPrincipal);
        return finalPrincipal;
    }

    public void setPrincipalFactory(final PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }
}
