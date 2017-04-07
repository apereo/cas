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

    private AuthenticationTypes type;
    private String principalAttributePassword;
    private String dnFormat;
    private boolean enhanceWithEntryResolver = true;

    private boolean subtreeSearch = true;
    private String baseDn;
    private String userFilter;

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
