package org.apereo.cas.configuration.model.support.saml;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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

    private static final long serialVersionUID = -8505851926931247878L;

    /**
     * Skew allowance that controls the issue instance of the authentication.
     */
    private int skewAllowance = 5;

    /**
     * Issue length that controls the validity period of the assertion.
     */
    private int issueLength = 30;

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
}
