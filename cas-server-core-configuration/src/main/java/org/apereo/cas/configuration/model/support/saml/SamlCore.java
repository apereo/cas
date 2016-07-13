package org.apereo.cas.configuration.model.support.saml;

/**
 * This is {@link SamlCore}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class SamlCore {
    private int skewAllowance;

    private String attributeNamespace = "http://www.ja-sig.org/products/cas/";

    private String issuer = "localhost";

    private boolean ticketidSaml2;
    
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

    public boolean isTicketidSaml2() {
        return ticketidSaml2;
    }

    public void setTicketidSaml2(final boolean ticketidSaml2) {
        this.ticketidSaml2 = ticketidSaml2;
    }
}
