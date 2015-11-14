package org.jasig.cas.services.web.beans;

import java.io.Serializable;

/**
 * Bean that defines user-name attribute providers.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServiceUsernameAttributeProviderEditBean implements Serializable {
    private static final long serialVersionUID = 3912289299527532705L;

    /**
     * The enum Types.
     */
    public enum Types {
        /**
         * default type.
         */
        DEFAULT("default"),

        /**
         * Attr type.
         */
        ATTRIBUTE("attr"),

        /**
         * anonymous type.
         */
        ANONYMOUS("anon");

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

    private String value;
    private String type = Types.DEFAULT.toString();

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }
}
