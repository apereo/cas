package org.apereo.cas.support.wsfederation;

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
     */
    void modifyAttributes(Map<String, List<Object>> attributes);
}
