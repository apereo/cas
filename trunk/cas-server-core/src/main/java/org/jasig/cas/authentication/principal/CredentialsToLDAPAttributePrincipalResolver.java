/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.principal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.services.AttributeRepository;
import org.jasig.cas.util.LdapUtils;
import org.jasig.cas.util.annotation.IsIn;
import org.jasig.cas.util.annotation.NotNull;

import org.springframework.ldap.AttributesMapper;
import org.springframework.ldap.LdapTemplate;
import org.springframework.ldap.support.LdapContextSource;

/**
 * @author Jan Van der Velpen
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @version $Revision:$ $Date:$
 * @since 3.1
 */
public final class CredentialsToLDAPAttributePrincipalResolver implements
    CredentialsToPrincipalResolver {

    /** The default maximum number of results to return. */
    private static final int DEFAULT_MAX_NUMBER_OF_RESULTS = 1000;

    /** The default timeout. */
    private static final int DEFAULT_TIMEOUT = 1000;

    /** Log instance. */
    protected final Log log = LogFactory.getLog(this.getClass());

    /**
     * The CredentialsToPrincipalResolver that resolves the principal from the
     * request
     */
    @NotNull
    private CredentialsToPrincipalResolver credentialsToPrincipalResolver;

    /** LdapTemplate to execute ldap queries. */
    @NotNull
    private LdapTemplate ldapTemplate;

    /** The filter path to the lookup value of the user. */
    @NotNull
    private String filter;

    /** The attribute that contains the value that should become the principal */
    @NotNull
    protected String principalAttributeName;

    /** The search base to find the user under. */
    @NotNull
    private String searchBase;

    /** The scope. */
    @IsIn({SearchControls.OBJECT_SCOPE, SearchControls.ONELEVEL_SCOPE,
        SearchControls.SUBTREE_SCOPE})
    private int scope = SearchControls.SUBTREE_SCOPE;

    /** The maximum number of results to return. */
    private int maxNumberResults = DEFAULT_MAX_NUMBER_OF_RESULTS;

    /** The amount of time to wait. */
    private int timeout = DEFAULT_TIMEOUT;
  
    /** Repository of principal attributes to be retrieved */
    private AttributeRepository attributeRepository;

    private SearchControls getSearchControls() {
        final SearchControls constraints = new SearchControls();
        final String[] attributeIds = this.attributeRepository != null
            ? getAttributeIds(this.principalAttributeName,
                this.attributeRepository.getAttributes())
            : new String[] {this.principalAttributeName};
        if (log.isDebugEnabled()) {
            log.debug("returning searchcontrols: scope=" + this.scope
                + "; search base=" + this.searchBase
                + "; attributes=" + attributesToString(attributeIds)
                + "; timeout=" + this.timeout
                + "; maxNumberResults=" + this.maxNumberResults);
        }
        constraints.setSearchScope(this.scope);
        constraints.setReturningAttributes(attributeIds);
        constraints.setTimeLimit(this.timeout);
        constraints.setCountLimit(this.maxNumberResults);
        return constraints;
    }

    public Principal resolvePrincipal(final Credentials credentials) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to resolve a principal...");
        }

        final Principal principal = this.credentialsToPrincipalResolver
            .resolvePrincipal(credentials);

        if (principal == null) {
            log.info("Initial principal could not be resolved from request, "
                + "returning null");
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("Resolved " + principal + ". Trying LDAP resolve now...");
        }

        final Principal ldapPrincipal = resolveFromLDAP(principal.getId());

        if (ldapPrincipal == null) {
            log.info("Initial principal \"" + principal.getId()
                + "\" was not found in LDAP, returning null");
        } else {
            log.debug("Resolved " + principal + " to " + ldapPrincipal);
        }

        return ldapPrincipal;
    }

    private Principal resolveFromLDAP(final String lookupAttributeValue) {
        final String searchFilter = LdapUtils.getFilterWithValues(this.filter,
            lookupAttributeValue);

        if (log.isDebugEnabled()) {
            log.debug("LDAP search with filter \"" + searchFilter + "\"");
        }
        try {
            // searching the directory
            final List principalList = this.ldapTemplate.search(
                this.searchBase, searchFilter, getSearchControls(),
                new PrincipalAttributesMapper(this.principalAttributeName));
            if (principalList.isEmpty()) {
                log.debug("LDAP search returned zero results.");
                return null;
            }
            if (principalList.size() > 1) {
                log.error("LDAP search returned multiple results "
                    + "for filter \"" + searchFilter + "\", "
                    + "which is not allowed.");

                return null;
            }
            return (Principal) principalList.get(0);

        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
    }
    
    private String[] getAttributeIds(String idAttribute,
        Collection<org.jasig.cas.services.Attribute> attributes) {
        
        final String[] attributeIds = new String[attributes.size()+1];
        attributeIds[0] = idAttribute;
        int i = 1;
        for (org.jasig.cas.services.Attribute attr : attributes) {
            attributeIds[i++] = attr.getId();
        }
        return attributeIds;
    }
    
    private String attributesToString(String[] attributes) {
        final StringBuilder sb = new StringBuilder();
        sb.append('{');
        int i = 0;
        for (String attr : attributes) {
            if (i++ > 0) {
                sb.append(',');
            }
            sb.append(attr);
        }
        sb.append('}');
        return sb.toString();
    }
    

    /*
     * Delegates checking to the configured CredentialsToPrincipalResolver.
     */
    public boolean supports(final Credentials credentials) {
        return this.credentialsToPrincipalResolver.supports(credentials);
    }

    /**
     * @param credentialsToPrincipalResolver The credentialsToPrincipalResolver
     * to set.
     */
    public final void setCredentialsToPrincipalResolver(
        CredentialsToPrincipalResolver credentialsToPrincipalResolver) {
        this.credentialsToPrincipalResolver = credentialsToPrincipalResolver;
    }

    /**
     * Method to set the datasource and generate a LDAPTemplate.
     * 
     * @param dataSource the datasource to use.
     */
    public final void setContextSource(final LdapContextSource contextSource) {
        this.ldapTemplate = new LdapTemplate(contextSource);
    }

    /**
     * @param filter The LDAP filter to set.
     */
    public void setFilter(final String filter) {
        this.filter = filter;
    }

    /**
     * @param principalAttributeName The principalAttributeName to set.
     */
    public final void setPrincipalAttributeName(final String principalAttributeName) {
        this.principalAttributeName = principalAttributeName;
    }

    /**
     * @param filter The scope to set.
     */
    public final void setScope(final int scope) {
        this.scope = scope;
    }

    /**
     * @param maxNumberResults The maxNumberResults to set.
     */
    public final void setMaxNumberResults(final int maxNumberResults) {
        this.maxNumberResults = maxNumberResults;
    }

    /**
     * @param searchBase The searchBase to set.
     */
    public final void setSearchBase(final String searchBase) {
        this.searchBase = searchBase;
    }

    /**
     * @param timeout The timeout to set.
     */
    public final void setTimeout(final int timeout) {
        this.timeout = timeout;
    }
   
    /**
     * @param repository Attribute repository containing attributes to fetch
     * about principal when credentials are resolved.
     */
    public final void setAttributeRepository(final AttributeRepository repository) {
        this.attributeRepository = repository;
    }
    
    class PrincipalAttributesMapper implements AttributesMapper {

        private String idAttribute;

        public PrincipalAttributesMapper(final String idAttribute) {
            this.idAttribute = idAttribute;
        }
        
        public Object mapFromAttributes(final Attributes attrs)
            throws NamingException {
            
            final Attribute attribute = attrs.get(this.idAttribute);
            if (attribute == null) {
                log.debug("Principal attribute \"" + this.idAttribute + "\" "
                    + "not found in LDAP search results. Returning null.");
                return null;
            }
            final String id = (String)attribute.get();
            final Map<String, Object> attrMap
                = new HashMap<String, Object>();
            final NamingEnumeration<String> names = attrs.getIDs();
            while (names.hasMoreElements()) {
                final String attrId = names.next();
                if (attrId.equals(this.idAttribute)) {
                    continue;
                }
                log.debug("Found LDAP attribute " + attrId);
                attrMap.put(attrId, getAttributeValues(attrs, attrId));
            }
            return new SimplePrincipal(id, attrMap);
        }
        
        private Object getAttributeValues(final Attributes attrs, 
            final String id) throws NamingException {
            final Attribute attr = attrs.get(id);
            
            if (attr.size() == 0) {
                return null;
            }

            final List<Object> list = new ArrayList<Object>();
            final NamingEnumeration ne = attr.getAll();

            while (ne.hasMoreElements()) {
                list.add(ne.next());
            }

            return list.size() > 1 ? list : list.get(0);
        }
    }
}
