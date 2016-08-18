package org.apereo.cas.mgmt.services.web.beans;

import java.io.Serializable;

/**
 * Proxy policy bean defined per JSON feed.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServiceProxyPolicyBean implements Serializable {

    private static final long serialVersionUID = 5990879744144480587L;

    /**
     * The enum AlgorithmTypes.
     */
    public enum Types {
        /** Refuse type. */
        REFUSE,

        /** Allow type. */
        REGEX
    }
    private Types type;
    private String value;

    public Types getType() {
        return this.type;
    }

    public void setType(final Types type) {
        this.type = type;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
