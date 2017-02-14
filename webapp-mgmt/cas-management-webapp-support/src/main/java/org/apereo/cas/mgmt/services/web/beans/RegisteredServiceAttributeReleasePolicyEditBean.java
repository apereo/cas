package org.apereo.cas.mgmt.services.web.beans;

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
        DEFAULT,

        /** Mapped type. */
        CACHED
    }

    /**
     * The enum AttributeMergerTypes.
     */
    public enum AttributeMergerTypes {
        /** default type. */
        DEFAULT,

        /** replace type. */
        REPLACE,

        /** multivalued type. */
        MULTIVALUED,

        /** add type. */
        ADD
    }

    private String attrFilter;
    private String cachedTimeUnit;
    private long cachedExpiration;
    private RegisteredServiceAttributeReleasePolicyStrategyEditBean attrPolicy =
            new RegisteredServiceAttributeReleasePolicyStrategyEditBean();
    private Types attrOption = Types.DEFAULT;
    private AttributeMergerTypes mergingStrategy = AttributeMergerTypes.DEFAULT;

    public String getAttrFilter() {
        return this.attrFilter;
    }

    public void setAttrFilter(final String attrFilter) {
        this.attrFilter = attrFilter;
    }

    public String getCachedTimeUnit() {
        return this.cachedTimeUnit;
    }

    public void setCachedTimeUnit(final String cachedTimeUnit) {
        this.cachedTimeUnit = cachedTimeUnit;
    }

    public long getCachedExpiration() {
        return this.cachedExpiration;
    }

    public void setCachedExpiration(final long cachedExpiration) {
        this.cachedExpiration = cachedExpiration;
    }

    public RegisteredServiceAttributeReleasePolicyStrategyEditBean getAttrPolicy() {
        return this.attrPolicy;
    }

    public void setAttrPolicy(final RegisteredServiceAttributeReleasePolicyStrategyEditBean attrPolicy) {
        this.attrPolicy = attrPolicy;
    }

    public Types getAttrOption() {
        return this.attrOption;
    }

    public void setAttrOption(final Types attrOption) {
        this.attrOption = attrOption;
    }

    public AttributeMergerTypes getMergingStrategy() {
        return this.mergingStrategy;
    }

    public void setMergingStrategy(final AttributeMergerTypes mergingStrategy) {
        this.mergingStrategy = mergingStrategy;
    }
}
