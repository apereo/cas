package org.jasig.cas.services.web.beans;

import java.io.Serializable;

/**
 * Abstract attribute release strategy bean.
 * @author Misagh Moayyed
 * @since 4.1
 */
public abstract class AbstractRegisteredServiceAttributeReleasePolicyStrategyBean implements Serializable {
    private static final long serialVersionUID = 8856759294453881982L;

    /**
     * The enum Types.
     */
    public enum Types {
        /** all type. */
        ALL("all"),

        /** Mapped type. */
        MAPPED("mapped"),

        /** None type. */
        NONE("none"),

        /** Allow type. */
        ALLOWED("allowed");

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
}
