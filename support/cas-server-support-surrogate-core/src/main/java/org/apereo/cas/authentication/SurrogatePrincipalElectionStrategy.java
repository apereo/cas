package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.Principal;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;

/**
 * This is {@link SurrogatePrincipalElectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class SurrogatePrincipalElectionStrategy extends DefaultPrincipalElectionStrategy {
    @Serial
    private static final long serialVersionUID = -3112906686072339162L;

    @Override
    protected Map<String, List<Object>> getPrincipalAttributesForPrincipal(final Collection<Authentication> authentications,
                                                                           final Principal principal,
                                                                           final Map<String, List<Object>> principalAttributes) {
        val result = authentications
            .stream()
            .map(Authentication::getPrincipal)
            .filter(SurrogatePrincipal.class::isInstance)
            .map(SurrogatePrincipal.class::cast)
            .findFirst();
        if (result.isPresent()) {
            return principal.getAttributes();
        }
        return super.getPrincipalAttributesForPrincipal(authentications, principal, principalAttributes);
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
    public @Nullable Principal nominate(final List<Principal> principals, final Map<String, List<Object>> attributes) throws Throwable {
        LOGGER.debug("Calculating principal from principals [{}]", principals);
        val result = principals
            .stream()
            .filter(SurrogatePrincipal.class::isInstance)
            .map(SurrogatePrincipal.class::cast)
            .findFirst();
        if (result.isPresent()) {
            val surrogate = result.get();
            
            principals.removeIf(SurrogatePrincipal.class::isInstance);
            val primaryAttributes = new LinkedHashMap<>(surrogate.getPrimary().getAttributes());
            principals.stream()
                .map(principal -> CoreAuthenticationUtils.mergeAttributes(primaryAttributes, principal.getAttributes(), getAttributeMerger()))
                .forEach(primaryAttributes::putAll);
            surrogate.getPrimary().getAttributes().putAll(primaryAttributes);
            LOGGER.debug("Nominated surrogate principal [{}]", surrogate);
            return surrogate;
        }
        return super.nominate(principals, attributes);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
