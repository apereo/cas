package org.apereo.cas.mgmt.services.web.beans;

import java.io.Serializable;

/**
 * Class used to serialize service information to be used when presenting
 * lists of services.
 *
 * @author Travis Schmidt
 * @since 5.2
 */
public class RegisteredServiceItem implements Serializable {

    private static final long serialVersionUID = 4882440567964605644L;

    private int evalOrder = Integer.MIN_VALUE;
    private String assignedId;
    private String serviceId;
    private String name;
    private String description;

    public int getEvalOrder() {
        return this.evalOrder;
    }

    public void setEvalOrder(final int evalOrder) {
        this.evalOrder = evalOrder;
    }

    public String getAssignedId() {
        return this.assignedId;
    }

    public void setAssignedId(final String assignedId) {
        this.assignedId = assignedId;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public void setServiceId(final String serviceId) {
        this.serviceId = serviceId;
    }

    public String getName() {
        return this.name;
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
}
