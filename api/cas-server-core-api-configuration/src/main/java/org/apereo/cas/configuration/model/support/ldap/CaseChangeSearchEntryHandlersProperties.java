package org.apereo.cas.configuration.model.support.ldap;

import java.io.Serializable;
import java.util.List;

/**
 * This is {@link CaseChangeSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CaseChangeSearchEntryHandlersProperties implements Serializable {
    private static final long serialVersionUID = 2420895955116725666L;
    /**
     * The Dn case change.
     */
    private String dnCaseChange;
    /**
     * The Attribute name case change.
     */
    private String attributeNameCaseChange;
    /**
     * The Attribute value case change.
     */
    private String attributeValueCaseChange;
    /**
     * The Attribute names.
     */
    private List<String> attributeNames;

    public String getDnCaseChange() {
        return dnCaseChange;
    }

    public void setDnCaseChange(final String dnCaseChange) {
        this.dnCaseChange = dnCaseChange;
    }

    public String getAttributeNameCaseChange() {
        return attributeNameCaseChange;
    }

    public void setAttributeNameCaseChange(final String attributeNameCaseChange) {
        this.attributeNameCaseChange = attributeNameCaseChange;
    }

    public String getAttributeValueCaseChange() {
        return attributeValueCaseChange;
    }

    public void setAttributeValueCaseChange(final String attributeValueCaseChange) {
        this.attributeValueCaseChange = attributeValueCaseChange;
    }

    public List<String> getAttributeNames() {
        return attributeNames;
    }

    public void setAttributeNames(final List<String> attributeNames) {
        this.attributeNames = attributeNames;
    }
}
