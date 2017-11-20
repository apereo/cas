package org.apereo.cas.configuration.model.support.ldap;

import java.io.Serializable;
import java.util.List;

/**
 * This is {@link MergeAttributesSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MergeAttributesSearchEntryHandlersProperties implements Serializable {
    private static final long serialVersionUID = -3988972992084584349L;
    /**
     * The Merge attribute name.
     */
    private String mergeAttributeName;
    /**
     * The Attribute names.
     */
    private List<String> attributeNames;

    public String getMergeAttributeName() {
        return mergeAttributeName;
    }

    public void setMergeAttributeName(final String mergeAttributeName) {
        this.mergeAttributeName = mergeAttributeName;
    }

    public List<String> getAttributeNames() {
        return attributeNames;
    }

    public void setAttributeNames(final List<String> attributeNames) {
        this.attributeNames = attributeNames;
    }
}
