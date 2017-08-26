package org.apereo.cas.configuration.model.support.ldap;

import java.io.Serializable;
import java.util.List;

/**
 * This is {@link RecursiveSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RecursiveSearchEntryHandlersProperties implements Serializable {
    private static final long serialVersionUID = 7038108925310792763L;
    /**
     * The Search attribute.
     */
    private String searchAttribute;
    /**
     * The Merge attributes.
     */
    private List<String> mergeAttributes;

    public String getSearchAttribute() {
        return searchAttribute;
    }

    public void setSearchAttribute(final String searchAttribute) {
        this.searchAttribute = searchAttribute;
    }

    public List<String> getMergeAttributes() {
        return mergeAttributes;
    }

    public void setMergeAttributes(final List<String> mergeAttributes) {
        this.mergeAttributes = mergeAttributes;
    }
}
