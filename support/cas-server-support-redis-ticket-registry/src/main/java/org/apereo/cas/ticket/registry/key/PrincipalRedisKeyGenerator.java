package org.apereo.cas.ticket.registry.key;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.registry.RedisCompositeKey;

/**
 * This is {@link PrincipalRedisKeyGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class PrincipalRedisKeyGenerator implements RedisKeyGenerator {

    @Override
    public String getType() {
        return Principal.class.getName();
    }

    @Override
    public String forAllEntries() {
        return RedisCompositeKey.forPrincipal().toKeyPattern();
    }

    @Override
    public String forEntry(final String type, final String entry) {
        return RedisCompositeKey.forPrincipal().withQuery(entry).toKeyPattern();
    }

    @Override
    public String getNamespace() {
        return RedisCompositeKey.forPrincipal().getPrefix();
    }
}
