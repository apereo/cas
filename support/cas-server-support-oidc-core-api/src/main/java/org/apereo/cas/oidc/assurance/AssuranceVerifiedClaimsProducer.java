package org.apereo.cas.oidc.assurance;

import org.jose4j.jwt.JwtClaims;
import java.util.Map;

/**
 * This is {@link AssuranceVerifiedClaimsProducer}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface AssuranceVerifiedClaimsProducer {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "assuranceVerifiedClaimsProducer";

    /**
     * Produce.
     *
     * @param jwtClaims      the jwt claims
     * @param claimName      the claim name
     * @param trustFramework the trust framework
     * @return the map
     */
    Map<String, Object> produce(JwtClaims jwtClaims, String claimName, String trustFramework);
}
