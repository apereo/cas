package org.apereo.cas.support.saml.web.idp.profile.builders.nameid;

import module java.base;
import org.apereo.cas.support.saml.SamlException;
import org.opensaml.saml.saml2.core.StatusCode;

/**
 * Thrown when the NameID format requested by the {@code NameIDPolicy} of an authentication request
 * cannot be satisfied, which the SAML2 response reports with the
 * {@code urn:oasis:names:tc:SAML:2.0:status:InvalidNameIDPolicy} status.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class SamlIdPInvalidNameIdPolicyException extends SamlException {
    @Serial
    private static final long serialVersionUID = -4127651904731859213L;

    public SamlIdPInvalidNameIdPolicyException(final String message) {
        super(StatusCode.INVALID_NAMEID_POLICY, message, List.of());
    }
}
