package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This is {@link SurrogatePrincipalElectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class SurrogatePrincipalElectionStrategy extends DefaultPrincipalElectionStrategy {
    private static final long serialVersionUID = -3112906686072339162L;

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    @Override
    protected Principal getPrincipalFromAuthentication(final Collection<Authentication> authentications) {
        val result = authentications
            .stream()
            .map(Authentication::getPrincipal)
            .filter(SurrogatePrincipal.class::isInstance)
            .map(SurrogatePrincipal.class::cast)
            .findFirst();
        if (result.isPresent()) {
            return result.get().getSurrogate();
        }
        return super.getPrincipalFromAuthentication(authentications);
    }

    @Override
    protected Map<String, List<Object>> getPrincipalAttributesForPrincipal(final Principal principal, final Map<String, List<Object>> principalAttributes) {
        return principal.getAttributes();
    }
}
