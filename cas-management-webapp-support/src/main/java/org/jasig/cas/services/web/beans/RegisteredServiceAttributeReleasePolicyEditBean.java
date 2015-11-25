package org.jasig.cas.services.web.beans;

import java.io.Serializable;

/**
 * Attribute release policy defined per JSON feed.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServiceAttributeReleasePolicyEditBean extends AbstractRegisteredServiceAttributeReleasePolicyBean
        implements Serializable {
    private static final long serialVersionUID = -7567470297544895709L;

    /**
     * The enum Types.
     */
    public enum Types {
        /** default type. */
        DEFAULT("default"),

        /** Mapped type. */
        CACHED("cached");

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

    /**
     * The enum AttributeMergerTypes.
     */
    public enum AttributeMergerTypes {
        /** default type. */
        DEFAULT("default"),

        /** replace type. */
        REPLACE("replace"),

        /** multivalued type. */
        MULTIVALUED("multivalued"),

        /** add type. */
        ADD("add");

        private final String value;

        /**
         * Instantiates a new AttributeMergerTypes.
         *
         * @param value the value
         */
        AttributeMergerTypes(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    private String attrFilter;
    private String cachedTimeUnit;
    private long cachedExpiration;
    private RegisteredServiceAttributeReleasePolicyStrategyEditBean attrPolicy =
            new RegisteredServiceAttributeReleasePolicyStrategyEditBean();
    private String attrOption = Types.DEFAULT.value;
    private String mergingStrategy = AttributeMergerTypes.DEFAULT.value;

    public String getAttrFilter() {
        return attrFilter;
    }

    public void setAttrFilter(final String attrFilter) {
        this.attrFilter = attrFilter;
    }

    public String getCachedTimeUnit() {
        return cachedTimeUnit;
    }

    public void setCachedTimeUnit(final String cachedTimeUnit) {
        this.cachedTimeUnit = cachedTimeUnit;
    }

    public long getCachedExpiration() {
        return cachedExpiration;
    }

    public void setCachedExpiration(final long cachedExpiration) {
        this.cachedExpiration = cachedExpiration;
    }

    public RegisteredServiceAttributeReleasePolicyStrategyEditBean getAttrPolicy() {
        return attrPolicy;
    }

    public void setAttrPolicy(final RegisteredServiceAttributeReleasePolicyStrategyEditBean attrPolicy) {
        this.attrPolicy = attrPolicy;
    }

    public String getAttrOption() {
        return attrOption;
    }

    public void setAttrOption(final String attrOption) {
        this.attrOption = attrOption;
    }

    public String getMergingStrategy() {
        return mergingStrategy;
    }

    public void setMergingStrategy(final String mergingStrategy) {
        this.mergingStrategy = mergingStrategy;
    }
}
