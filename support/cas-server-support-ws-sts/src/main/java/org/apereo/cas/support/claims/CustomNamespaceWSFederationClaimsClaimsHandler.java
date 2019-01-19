package org.apereo.cas.support.claims;

import org.apereo.cas.ws.idp.WSFederationConstants;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.cxf.rt.security.claims.Claim;
import org.apache.cxf.sts.claims.ClaimsParameters;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CustomNamespaceWSFederationClaimsClaimsHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class CustomNamespaceWSFederationClaimsClaimsHandler extends NonWSFederationClaimsClaimsHandler {
    private final List<URI> supportedClaimTypes;

    public CustomNamespaceWSFederationClaimsClaimsHandler(final String handlerRealm, final String issuer,
                                                          final List<String> namespaces) {
        super(handlerRealm, issuer);
        this.supportedClaimTypes = new NonWSFederationClaimsList(namespaces);
    }

    @SneakyThrows
    @Override
    protected URI createProcessedClaimType(final Claim requestClaim, final ClaimsParameters parameters) {
        val tokenType = parameters.getTokenRequirements().getTokenType();
        if (WSFederationConstants.WSS_SAML2_TOKEN_TYPE.equalsIgnoreCase(tokenType)) {
            val claimType = requestClaim.getClaimType().toASCIIString();
            val idx = claimType.lastIndexOf('/');
            val claimName = claimType.substring(idx + 1).trim();
            LOGGER.debug("Converted full claim type from [{}] to [{}]", claimType, claimName);
            return new URI(claimName);
        }
        return requestClaim.getClaimType();
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
            val uri = ((URI) o).toASCIIString();
            return namespaces.stream().anyMatch(uri::startsWith);
        }
    }
}
