package org.apereo.cas.mgmt.services.web.beans;

import java.io.Serializable;

/**
 * Class used to serialize Service Details to the client.
 *
 * @author Travis Schmidt
 * @since 5.2
 */
public class RegisteredServiceDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private String description;
    private String proxyPolicy;
    private String proxyPolicyValue;
    private String attributePolicy;
    private String releaseCredential;
    private String releaseProxyTicket;
    private String logoUrl;

    public RegisteredServiceDetails() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getProxyPolicy() {
        return proxyPolicy;
    }

    public void setProxyPolicy(final String proxyPolicy) {
        this.proxyPolicy = proxyPolicy;
    }

    public String getAttributePolicy() {
        return attributePolicy;
    }

    public void setAttributePolicy(final String attributePolicy) {
        this.attributePolicy = attributePolicy;
    }

    public String getReleaseCredential() {
        return releaseCredential;
    }

    public void setReleaseCredential(final String releaseCredential) {
        this.releaseCredential = releaseCredential;
    }

    public String getReleaseProxyTicket() {
        return releaseProxyTicket;
    }

    public void setReleaseProxyTicket(final String releaseProxyTicket) {
        this.releaseProxyTicket = releaseProxyTicket;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(final String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getProxyPolicyValue() {
        return proxyPolicyValue;
    }

    public void setProxyPolicyValue(final String proxyPolicyValue) {
        this.proxyPolicyValue = proxyPolicyValue;
    }
}
