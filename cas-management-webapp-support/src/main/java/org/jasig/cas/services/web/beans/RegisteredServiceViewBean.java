package org.jasig.cas.services.web.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines the service bean that is produced by the webapp
 * and passed down.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServiceViewBean implements Serializable {

    private static final long serialVersionUID = 4882440567964605644L;

    private int evalOrder = Integer.MIN_VALUE;
    private long assignedId;
    private boolean sasCASEnabled;
    private String serviceId;
    private String name;
    private String description;
    private String logoUrl;
    private RegisteredServiceProxyPolicyBean proxyPolicy = new RegisteredServiceProxyPolicyBean();
    private RegisteredServiceAttributeReleasePolicyViewBean attrRelease = new RegisteredServiceAttributeReleasePolicyViewBean();

    /**
     * This is reserved for usage by any custom components that need to present their config to the management UI. This
     * should only contain nested Maps and Arrays of simple values.
     */
    private Map<String, Object> customComponent = new HashMap<>();

    public int getEvalOrder() {
        return evalOrder;
    }

    public void setEvalOrder(final int evalOrder) {
        this.evalOrder = evalOrder;
    }

    public long getAssignedId() {
        return assignedId;
    }

    public void setAssignedId(final long assignedId) {
        this.assignedId = assignedId;
    }

    public boolean isSasCASEnabled() {
        return sasCASEnabled;
    }

    public void setSasCASEnabled(final boolean sasCASEnabled) {
        this.sasCASEnabled = sasCASEnabled;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(final String serviceId) {
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(final String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public RegisteredServiceProxyPolicyBean getProxyPolicy() {
        return proxyPolicy;
    }

    public void setProxyPolicy(final RegisteredServiceProxyPolicyBean proxyPolicy) {
        this.proxyPolicy = proxyPolicy;
    }

    public RegisteredServiceAttributeReleasePolicyViewBean getAttrRelease() {
        return attrRelease;
    }

    public void setAttrRelease(final RegisteredServiceAttributeReleasePolicyViewBean attrRelease) {
        this.attrRelease = attrRelease;
    }

    public Map<String, Object> getCustomComponent() {
        return customComponent;
    }

    public void setCustomComponent(final Map<String, Object> customComponent) {
        this.customComponent = customComponent;
    }
}
