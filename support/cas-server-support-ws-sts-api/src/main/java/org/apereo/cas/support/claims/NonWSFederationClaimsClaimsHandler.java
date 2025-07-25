package org.apereo.cas.support.claims;

import org.apereo.cas.ws.idp.WSFederationConstants;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.apache.cxf.rt.security.claims.Claim;
import org.apache.cxf.sts.claims.ClaimsParameters;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link NonWSFederationClaimsClaimsHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class NonWSFederationClaimsClaimsHandler extends WrappingSecurityTokenServiceClaimsHandler {
    public NonWSFederationClaimsClaimsHandler(final String handlerRealm, final String issuer) {
        super(handlerRealm, issuer);
    }

    @Override
    protected String createProcessedClaimType(final Claim requestClaim, final ClaimsParameters parameters) {
        val tokenType = parameters.getTokenRequirements().getTokenType();
        if (WSFederationConstants.WSS_SAML2_TOKEN_TYPE.equalsIgnoreCase(tokenType)) {
            return Strings.CI.remove(requestClaim.getClaimType(), WSFederationConstants.HTTP_SCHEMAS_APEREO_CAS);
        }
        return requestClaim.getClaimType();
    }

    @Override
    public List<String> getSupportedClaimTypes() {
        return new NonWSFederationClaimsList();
    }

    private static final class NonWSFederationClaimsList extends ArrayList<String> {
        @Serial
        private static final long serialVersionUID = -50278523307446738L;

        @Override
        public boolean contains(final Object o) {
            return o.toString().startsWith(WSFederationConstants.HTTP_SCHEMAS_APEREO_CAS);
        }
    }
}
