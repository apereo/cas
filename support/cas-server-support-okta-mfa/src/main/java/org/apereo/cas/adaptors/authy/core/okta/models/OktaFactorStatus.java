package org.apereo.cas.adaptors.authy.core.okta.models;

public class OktaFactorStatus {

    private final String factorStatus;
    private String factorId;

    public OktaFactorStatus(String factorStatus) {
        this.factorStatus = factorStatus;
    }

    public String getFactorStatus() {
        return factorStatus;
    }

    public String getFactorId() {
        return factorId;
    }

    public void setFactorId(String factorId) {
        this.factorId = factorId;
    }
}
