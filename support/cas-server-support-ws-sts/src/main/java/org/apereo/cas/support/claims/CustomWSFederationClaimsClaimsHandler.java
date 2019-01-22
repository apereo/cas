package org.apereo.cas.support.claims;

import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CustomWSFederationClaimsClaimsHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class CustomWSFederationClaimsClaimsHandler extends NonWSFederationClaimsClaimsHandler {
    private final List<URI> supportedClaimTypes;

    public CustomWSFederationClaimsClaimsHandler(final String handlerRealm, final String issuer,
                                                 final List<String> namespaces) {
        super(handlerRealm, issuer);
        this.supportedClaimTypes = new NonWSFederationClaimsList(namespaces);
    }

    @Override
    public List<URI> getSupportedClaimTypes() {
        return this.supportedClaimTypes;
    }

    @RequiredArgsConstructor
    private static class NonWSFederationClaimsList extends ArrayList<URI> {
        private static final long serialVersionUID = 8368878016992806802L;
        private final List<String> namespaces;

        @Override
        public boolean contains(final Object o) {
            final String uri = ((URI) o).toASCIIString();
            return namespaces.stream().anyMatch(uri::startsWith);
        }
    }
}
