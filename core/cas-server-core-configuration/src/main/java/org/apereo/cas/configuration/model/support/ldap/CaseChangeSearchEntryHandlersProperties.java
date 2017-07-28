package org.apereo.cas.configuration.model.support.ldap;

import org.ldaptive.handler.CaseChangeEntryHandler;

import java.io.Serializable;

/**
 * This is {@link CaseChangeSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CaseChangeSearchEntryHandlersProperties implements Serializable {
    private CaseChangeEntryHandler.CaseChange dnCaseChange;
    private CaseChangeEntryHandler.CaseChange attributeNameCaseChange;
    private CaseChangeEntryHandler.CaseChange attributeValueCaseChange;
    private String[] attributeNames;

    public CaseChangeEntryHandler.CaseChange getDnCaseChange() {
        return dnCaseChange;
    }

    public void setDnCaseChange(final CaseChangeEntryHandler.CaseChange dnCaseChange) {
        this.dnCaseChange = dnCaseChange;
    }

    public CaseChangeEntryHandler.CaseChange getAttributeNameCaseChange() {
        return attributeNameCaseChange;
    }

    public void setAttributeNameCaseChange(final CaseChangeEntryHandler.CaseChange attributeNameCaseChange) {
        this.attributeNameCaseChange = attributeNameCaseChange;
    }

    public CaseChangeEntryHandler.CaseChange getAttributeValueCaseChange() {
        return attributeValueCaseChange;
    }

    public void setAttributeValueCaseChange(final CaseChangeEntryHandler.CaseChange attributeValueCaseChange) {
        this.attributeValueCaseChange = attributeValueCaseChange;
    }

    public String[] getAttributeNames() {
        return attributeNames;
    }

    public void setAttributeNames(final String[] attributeNames) {
        this.attributeNames = attributeNames;
    }
}
