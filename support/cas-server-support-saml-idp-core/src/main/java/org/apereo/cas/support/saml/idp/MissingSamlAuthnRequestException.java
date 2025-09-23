package org.apereo.cas.support.saml.idp;

import org.apereo.cas.authentication.RootCasException;
import java.io.Serial;

/**
 * This is {@link MissingSamlAuthnRequestException}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class MissingSamlAuthnRequestException extends RootCasException {

    /**
     * Code description.
     */
    public static final String CODE = "MISSING_SAML_REQUEST";
    
    @Serial
    private static final long serialVersionUID = 585805040771717819L;
    
    public MissingSamlAuthnRequestException(final String message) {
        super(CODE, message);
    }
}
