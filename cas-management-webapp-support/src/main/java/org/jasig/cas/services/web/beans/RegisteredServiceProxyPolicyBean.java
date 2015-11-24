package org.jasig.cas.services.web.beans;

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
        REFUSE("refuse"),

        /** Allow type. */
        REGEX("regex");

        private final String value;

        /**
         * Instantiates a new AlgorithmTypes.
         *
         * @param value the value
         */
        Types(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
    private String type;
    private String value;

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
