package org.apereo.cas.support.wsfederation.attributes;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * This interface provides a mechanism to alter the SAML attributes before they
 * are added the WsFederationCredentials and returned to CAS.
 *
 * @author John Gasper
 * @since 4.2.0
 */
@FunctionalInterface
public interface WsFederationAttributeMutator extends Serializable {
    /**
     * modifyAttributes manipulates the attributes before they
     * are assigned to the credential.
     *
     * @param attributes the attribute returned by the IdP.
     * @return the map
     */
    Map<String, List<Object>> modifyAttributes(Map<String, List<Object>> attributes);

    /**
     * NoOp ws federation attribute mutator.
     *
     * @return the ws federation attribute mutator
     */
    static WsFederationAttributeMutator noOp() {
        return (WsFederationAttributeMutator) attributes -> attributes;
    }
}
