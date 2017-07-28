package org.apereo.cas.configuration.model.support.ldap;

import org.ldaptive.handler.CaseChangeEntryHandler;

import java.io.Serializable;

/**
 * This is {@link LdapSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class LdapSearchEntryHandlersProperties {
    /**
     * The enum Search entry handler types.
     */
    public enum SearchEntryHandlerTypes {
        /**
         * Object guid search entry handler.
         */
        OBJECT_GUID,
        /**
         * Object sid search entry handler.
         */
        OBJECT_SID,
        /**
         * Case change search entry handler.
         */
        CASE_CHANGE,
        /**
         * DN attribute entry handler.
         */
        DN_ATTRIBUTE_ENTRY,
        /**
         * Merge search entry handler.
         */
        MERGE,
        /**
         * Primary group search handler.
         */
        PRIMARY_GROUP,
        /**
         * Range entry search handler.
         */
        RANGE_ENTRY,
        /**
         * Recursive entry search handler.
         */
        RECURSIVE_ENTRY,
    }

    private SearchEntryHandlerTypes type;

    private CaseChangeSearchEntryHandlersProperties casChange;
    private DnAttributeSearchEntryHandlersProperties dnAttribute;
    private MergeAttributesSearchEntryHandlersProperties mergeAttribute;
    private PrimaryGroupIdSearchEntryHandlersProperties primaryGroupId;
    private RecursiveSearchEntryHandlersProperties recursive;

    public CaseChangeSearchEntryHandlersProperties getCasChange() {
        return casChange;
    }

    public void setCasChange(final CaseChangeSearchEntryHandlersProperties casChange) {
        this.casChange = casChange;
    }

    public DnAttributeSearchEntryHandlersProperties getDnAttribute() {
        return dnAttribute;
    }

    public void setDnAttribute(final DnAttributeSearchEntryHandlersProperties dnAttribute) {
        this.dnAttribute = dnAttribute;
    }

    public MergeAttributesSearchEntryHandlersProperties getMergeAttribute() {
        return mergeAttribute;
    }

    public void setMergeAttribute(final MergeAttributesSearchEntryHandlersProperties mergeAttribute) {
        this.mergeAttribute = mergeAttribute;
    }

    public PrimaryGroupIdSearchEntryHandlersProperties getPrimaryGroupId() {
        return primaryGroupId;
    }

    public void setPrimaryGroupId(final PrimaryGroupIdSearchEntryHandlersProperties primaryGroupId) {
        this.primaryGroupId = primaryGroupId;
    }

    public RecursiveSearchEntryHandlersProperties getRecursive() {
        return recursive;
    }

    public void setRecursive(final RecursiveSearchEntryHandlersProperties recursive) {
        this.recursive = recursive;
    }

    public SearchEntryHandlerTypes getType() {
        return type;
    }

    public void setType(final SearchEntryHandlerTypes type) {
        this.type = type;
    }


}
