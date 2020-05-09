package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.Principal;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.Ordered;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This is {@link SurrogatePrincipalElectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class SurrogatePrincipalElectionStrategy extends DefaultPrincipalElectionStrategy {
    private static final long serialVersionUID = -3112906686072339162L;

    @Override
    protected Map<String, List<Object>> getPrincipalAttributesForPrincipal(final Principal principal, final Map<String, List<Object>> principalAttributes) {
        return principal.getAttributes();
    }

    @Override
    protected Principal getPrincipalFromAuthentication(final Collection<Authentication> authentications) {
        LOGGER.trace("Calculating principal from authentications [{}]", authentications);
        val result = authentications
            .stream()
            .map(Authentication::getPrincipal)
            .filter(SurrogatePrincipal.class::isInstance)
            .map(SurrogatePrincipal.class::cast)
            .findFirst();
        if (result.isPresent()) {
            val surrogate = result.get().getSurrogate();
            LOGGER.debug("Found surrogate principal [{}]", surrogate);
            return surrogate;
        }
        return super.getPrincipalFromAuthentication(authentications);
    }

    @Override
    public Principal nominate(final List<Principal> principals, final Map<String, List<Object>> attributes) {
        LOGGER.trace("Calculating principal from principals [{}]", principals);
        val result = principals
            .stream()
            .filter(SurrogatePrincipal.class::isInstance)
            .map(SurrogatePrincipal.class::cast)
            .findFirst();
        if (result.isPresent()) {
            val surrogate = result.get();
            LOGGER.debug("Found surrogate principal [{}]", surrogate);
            return surrogate;
        }
        return super.nominate(principals, attributes);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
