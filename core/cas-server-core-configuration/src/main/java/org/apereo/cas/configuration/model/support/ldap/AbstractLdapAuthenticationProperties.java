package org.apereo.cas.configuration.model.support.ldap;

import org.ldaptive.handler.CaseChangeEntryHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link AbstractLdapAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractLdapAuthenticationProperties extends AbstractLdapProperties {

    private static final long serialVersionUID = 3849857270054289852L;

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

    /**
     * The enum Authentication types.
     */
    public enum AuthenticationTypes {
        /**
         * Active Directory.
         */
        AD,
        /**
         * Authenticated Search.
         */
        AUTHENTICATED,
        /**
         * Direct Bind.
         */
        DIRECT,
        /**
         * Anonymous Search.
         */
        ANONYMOUS
    }

    /**
     * The authentication type.
     * <ul>
     * <li>Active Directory - Users authenticate with sAMAccountName. </li>
     * <li>Authenticated Search - Manager bind/search</li>
     * <li>Anonymous Search</li>
     * <li>Direct Bind: Direct Bind - Compute user DN from format string and perform simple bind.
     * This is relevant when no search is required to compute the DN needed for a bind operation.
     * Use cases for this type are:
     * 1) All users are under a single branch in the directory, <code>e.g. ou=Users,dc=example,dc=org.</code>
     * 2) The username provided on the CAS login form is part of the DN, e.g.
     * <code>uid=%s,ou=Users,dc=example,dc=org</code>.</li>
     * </ul>
     */
    private AuthenticationTypes type;

    /**
     * If principalAttributePassword is empty then a user simple bind is done to validate credentials
     * otherwise the given attribute is compared with the given principalAttributePassword
     * using the SHA encrypted value of it.
     * <p>
     * For the anonymous authentication type,
     * if principalAttributePassword is empty then a user simple bind is done to validate credentials
     * otherwise the given attribute is compared with the given principalAttributePassword
     * using the SHA encrypted value of it.
     * </p>
     */
    private String principalAttributePassword;
    /**
     * Specify the dn format accepted by the AD authenticator, etc.
     * Example format might be <code>uid=%s,ou=people,dc=example,dc=org</code>.
     */
    private String dnFormat;
    /**
     * Whether specific search entry resolvers need to be set
     * on the authenticator, or the default should be used.
     */
    private boolean enhanceWithEntryResolver = true;

    /**
     * Whether subtree searching is allowed.
     */
    private boolean subtreeSearch = true;
    /**
     * Base DN to use.
     */
    private String baseDn;
    /**
     * User filter to use for searching.
     * Syntax is <code>cn={user}</code> or <code>cn={0}</code>.
     */
    private String userFilter;

    /**
     * Search entry to define on the authenticator.
     */
    private List<SearchEntryHandlers> searchEntryHandlers = new ArrayList<>();

    public List<SearchEntryHandlers> getSearchEntryHandlers() {
        return searchEntryHandlers;
    }

    public void setSearchEntryHandlers(final List<SearchEntryHandlers> searchEntryHandlers) {
        this.searchEntryHandlers = searchEntryHandlers;
    }

    public AuthenticationTypes getType() {
        return type;
    }

    public void setType(final AuthenticationTypes type) {
        this.type = type;
    }

    public boolean isSubtreeSearch() {
        return subtreeSearch;
    }

    public void setSubtreeSearch(final boolean subtreeSearch) {
        this.subtreeSearch = subtreeSearch;
    }

    public String getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(final String baseDn) {
        this.baseDn = baseDn;
    }

    public String getUserFilter() {
        return userFilter;
    }

    public void setUserFilter(final String userFilter) {
        this.userFilter = userFilter;
    }

    public String getDnFormat() {
        return dnFormat;
    }

    public void setDnFormat(final String dnFormat) {
        this.dnFormat = dnFormat;
    }

    public boolean isEnhanceWithEntryResolver() {
        return enhanceWithEntryResolver;
    }

    public void setEnhanceWithEntryResolver(final boolean enhanceWithEntryResolver) {
        this.enhanceWithEntryResolver = enhanceWithEntryResolver;
    }

    public String getPrincipalAttributePassword() {
        return principalAttributePassword;
    }

    public void setPrincipalAttributePassword(final String principalAttributePassword) {
        this.principalAttributePassword = principalAttributePassword;
    }


    public static class SearchEntryHandlers {
        private SearchEntryHandlerTypes type;

        private CaseChange casChange;
        private DnAttribute dnAttribute;
        private MergeAttribute mergeAttribute;
        private PrimaryGroupId primaryGroupId;
        private Recursive recursive;

        public CaseChange getCasChange() {
            return casChange;
        }

        public void setCasChange(final CaseChange casChange) {
            this.casChange = casChange;
        }

        public DnAttribute getDnAttribute() {
            return dnAttribute;
        }

        public void setDnAttribute(final DnAttribute dnAttribute) {
            this.dnAttribute = dnAttribute;
        }

        public MergeAttribute getMergeAttribute() {
            return mergeAttribute;
        }

        public void setMergeAttribute(final MergeAttribute mergeAttribute) {
            this.mergeAttribute = mergeAttribute;
        }

        public PrimaryGroupId getPrimaryGroupId() {
            return primaryGroupId;
        }

        public void setPrimaryGroupId(final PrimaryGroupId primaryGroupId) {
            this.primaryGroupId = primaryGroupId;
        }

        public Recursive getRecursive() {
            return recursive;
        }

        public void setRecursive(final Recursive recursive) {
            this.recursive = recursive;
        }

        public SearchEntryHandlerTypes getType() {
            return type;
        }

        public void setType(final SearchEntryHandlerTypes type) {
            this.type = type;
        }

        public static class CaseChange {
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

        public static class DnAttribute {
            private String dnAttributeName = "entryDN";
            private boolean addIfExists;

            public String getDnAttributeName() {
                return dnAttributeName;
            }

            public void setDnAttributeName(final String dnAttributeName) {
                this.dnAttributeName = dnAttributeName;
            }

            public boolean isAddIfExists() {
                return addIfExists;
            }

            public void setAddIfExists(final boolean addIfExists) {
                this.addIfExists = addIfExists;
            }
        }

        public static class MergeAttribute {
            private String mergeAttributeName;
            private String[] attributeNames;

            public String getMergeAttributeName() {
                return mergeAttributeName;
            }

            public void setMergeAttributeName(final String mergeAttributeName) {
                this.mergeAttributeName = mergeAttributeName;
            }

            public String[] getAttributeNames() {
                return attributeNames;
            }

            public void setAttributeNames(final String[] attributeNames) {
                this.attributeNames = attributeNames;
            }
        }

        public static class PrimaryGroupId {
            private String groupFilter = "(&(objectClass=group)(objectSid={0}))";
            private String baseDn;

            public String getGroupFilter() {
                return groupFilter;
            }

            public void setGroupFilter(final String groupFilter) {
                this.groupFilter = groupFilter;
            }

            public String getBaseDn() {
                return baseDn;
            }

            public void setBaseDn(final String baseDn) {
                this.baseDn = baseDn;
            }
        }

        public static class Recursive {
            private String searchAttribute;
            private String[] mergeAttributes;

            public String getSearchAttribute() {
                return searchAttribute;
            }

            public void setSearchAttribute(final String searchAttribute) {
                this.searchAttribute = searchAttribute;
            }

            public String[] getMergeAttributes() {
                return mergeAttributes;
            }

            public void setMergeAttributes(final String[] mergeAttributes) {
                this.mergeAttributes = mergeAttributes;
            }
        }
    }
}
