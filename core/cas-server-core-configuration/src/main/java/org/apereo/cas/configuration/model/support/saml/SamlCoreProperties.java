package org.apereo.cas.configuration.model.support.saml;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link SamlCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-saml")
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
    private String securityManager = "com.sun.org.apache.xerces.internal.util.SecurityManager";

    public String getSecurityManager() {
        return securityManager;
    }

    public void setSecurityManager(final String securityManager) {
        this.securityManager = securityManager;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(final String issuer) {
        this.issuer = issuer;
    }

    public String getAttributeNamespace() {
        return attributeNamespace;
    }

    public void setAttributeNamespace(final String attributeNamespace) {
        this.attributeNamespace = attributeNamespace;
    }

    public int getSkewAllowance() {
        return skewAllowance;
    }

    public void setSkewAllowance(final int skewAllowance) {
        this.skewAllowance = skewAllowance;
    }

    public int getIssueLength() {
        return issueLength;
    }

    public void setIssueLength(final int issueLength) {
        this.issueLength = issueLength;
    }

    public boolean isTicketidSaml2() {
        return ticketidSaml2;
    }

    public void setTicketidSaml2(final boolean ticketidSaml2) {
        this.ticketidSaml2 = ticketidSaml2;
    }
}
