package org.apereo.cas.configuration.model.support.saml;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link SamlCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-saml")
@Getter
@Setter
@Accessors(chain = true)
public class SamlCoreProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -8505851926931247878L;

    /**
     * Skew allowance that controls the issue instance of the authentication.
     */
    @DurationCapable
    private String skewAllowance = "PT30S";

    /**
     * Issue length that controls the validity period of the assertion.
     */
    @DurationCapable
    private String issueLength = "PT30S";

    /**
     * Attribute namespace to use when generating SAML1 responses.
     */
    private String attributeNamespace = "http://www.ja-sig.org/products/cas/";

    /**
     * Issuer of the assertion when generating SAML1 responses.
     */
    private String issuer = "localhost";

    /**
     * Whether ticket ids generated should be saml2 compliant when generating SAML1 responses.
     */
    private boolean ticketidSaml2;

    /**
     * Qualified name of the security manager class used for creating a SAML parser pool.
     */
    private String securityManager = "org.apache.xerces.util.SecurityManager";

    /**
     * Salt used to generate persistent or transient ids
     * in particular when generating SAML2 responses or logout requests, etc.
     * When left undefined, CAS will generate a random salt. For production use,
     * it is recommended to define a salt value particularly when multiple CAS nodes are involved.
     */
    private String persistentIdSalt;
}
