package org.apereo.cas.adaptors.authy;

import org.apereo.cas.authentication.principal.Principal;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link InMemoryAuthyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class InMemoryAuthyAccountRegistry implements AuthyAccountRegistry {
    private Map<Principal, Integer> store = new HashMap<>();
    
    @Override
    public void add(final Integer authyId, final Principal principal) {
        store.put(principal, authyId);    
    }

    @Override
    public Integer get(final Principal principal) {
        return store.get(principal);
    }

    @Override
    public boolean contains(final Principal principal) {
        return store.containsKey(principal);
    }
}
