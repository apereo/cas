package org.apereo.cas.configuration.model.support.saml;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link SamlResponseProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class SamlResponseProperties {
    private int skewAllowance;

    private String attributeNamespace = "http://www.ja-sig.org/products/cas/";

    private String issuer = "localhost";

    private boolean ticketidSaml2;
    
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
