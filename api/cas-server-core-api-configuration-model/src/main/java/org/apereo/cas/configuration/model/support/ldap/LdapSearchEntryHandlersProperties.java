package org.apereo.cas.configuration.model.support.ldap;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link LdapSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-ldap")
@Getter
@Setter
@Accessors(chain = true)
public class LdapSearchEntryHandlersProperties implements Serializable {

    private static final long serialVersionUID = -5198990160347131821L;
    /**
     * The type of search entry handler to choose.
     * Accepted values are {@code OBJECT_GUID,OBJECT_SID,CASE_CHANGE,DN_ATTRIBUTE_ENTRY,MERGE,PRIMARY_GROUP,RANGE_ENTRY,RECURSIVE_ENTRY}
     */
    private SearchEntryHandlerTypes type;
    /**
     * Provides the ability to modify the case of search entry DNs, attribute names, and attribute values.
     */
    @NestedConfigurationProperty
    private CaseChangeSearchEntryHandlersProperties caseChange = new CaseChangeSearchEntryHandlersProperties();
    /**
     * Adds the entry DN as an attribute to the result set. Provides a client side implementation of RFC 5020.
     */
    @NestedConfigurationProperty
    private DnAttributeSearchEntryHandlersProperties dnAttribute = new DnAttributeSearchEntryHandlersProperties();
    /**
     * Merges the values of one or more attributes into a single attribute. The merged attribute may or may not already
     * exist on the entry. If it does exist it's existing values will remain intact.
     */
    @NestedConfigurationProperty
    private MergeAttributesSearchEntryHandlersProperties mergeAttribute = new MergeAttributesSearchEntryHandlersProperties();
    /**
     * Constructs the primary group SID and then searches for that group and puts it's DN in the 'memberOf' attribute of the
     * original search entry. This handler requires that entries contain both the 'objectSid' and 'primaryGroupID'
     * attributes. If those attributes are not found this handler is a no-op. This handler should be used in conjunction
     * with the {@code ObjectSidHandler} to ensure the 'objectSid' attribute is in the proper form. See
     * http://support2.microsoft.com/kb/297951
     */
    @NestedConfigurationProperty
    private PrimaryGroupIdSearchEntryHandlersProperties primaryGroupId = new PrimaryGroupIdSearchEntryHandlersProperties();
    /**
     * This recursively searches based on a supplied attribute and merges those results into the original entry.
     */
    @NestedConfigurationProperty
    private RecursiveSearchEntryHandlersProperties recursive = new RecursiveSearchEntryHandlersProperties();

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
        RECURSIVE_ENTRY
    }
}
