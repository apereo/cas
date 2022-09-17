package org.apereo.cas.support.spnego;

import java.security.Principal;

/**
 * @author Marc-Antoine Garrigue
 * @author Arnaud Lesueur
 * @since 3.1
 */
public record MockPrincipal(String name) implements Principal {
    @Override
    public String getName() {
        return name();
    }
}
