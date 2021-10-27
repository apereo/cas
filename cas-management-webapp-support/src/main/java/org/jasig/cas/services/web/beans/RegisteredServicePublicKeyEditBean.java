package org.jasig.cas.services.web.beans;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * Registered service public key options.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServicePublicKeyEditBean implements Serializable {
    private static final long serialVersionUID = 2553270792452015226L;

    private String location;
    private String algorithm = "RSA";

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(final String algorithm) {
        this.algorithm = algorithm;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(this.algorithm)
                && StringUtils.isNotBlank(this.location);
    }
}
