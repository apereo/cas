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
    private String assignedId;
    private boolean sasCASEnabled;
    private String serviceId;
    private String name;
    private String description;
    private String logoUrl;
    private RegisteredServiceProxyPolicyBean proxyPolicy = new RegisteredServiceProxyPolicyBean();
    private RegisteredServiceAttributeReleasePolicyViewBean attrRelease = new RegisteredServiceAttributeReleasePolicyViewBean();
    private Map<String, Map<String, ?>> customComponent = new HashMap<>();

    public int getEvalOrder() {
        return evalOrder;
    }

    public void setEvalOrder(final int evalOrder) {
        this.evalOrder = evalOrder;
    }

    public String getAssignedId() {
        return assignedId;
    }

    public void setAssignedId(final String assignedId) {
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

    /**
     * Visible for serialization only. Use {@link RegisteredServiceViewBean#getCustomComponent(String)} instead.
     *
     * @return all the custom components
     */
    public Map<String, Map<String, ?>> getCustomComponent() {
        return customComponent;
    }

    /**
     * Get the current properties for the specified custom component. The returned {@link Map} should only contain
     * nested Maps, Arrays, and simple objects.
     *
     * @param componentName name of the component to get the properties for (this should be unique for each component)
     * @return current custom component properties
     */
    public Map<String, ?> getCustomComponent(final String componentName) {
        return customComponent.get(componentName);
    }

    /**
     * This is reserved for usage by any custom components that need to present their config to the management UI. The
     * provided {@link Map} should only contain nested Maps, Arrays, and simple objects.
     *
     * @param componentName name of the component to store the properties for (this should be unique for each component)
     * @param properties    custom component properties
     */
    public void setCustomComponent(final String componentName, final Map<String, ?> properties) {
        this.customComponent.put(componentName, properties);
    }
}
