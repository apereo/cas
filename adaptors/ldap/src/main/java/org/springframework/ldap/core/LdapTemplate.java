/*
 * Copyright 2002-2004 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package org.springframework.ldap.core;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.springframework.dao.DataAccessException;
import org.springframework.ldap.support.CanonicalLdapExceptionTranslator;
import org.springframework.ldap.support.ContextSource;
import org.springframework.ldap.support.LdapExceptionTranslator;
import org.springframework.ldap.support.LdapUtils;

/**
 * <b>This is the central class in the LDAP core package. </b> It simplifies the use of LDAP and help to avoid common errors. It executes core LDAP
 * workflow, leaving application code to provide LDAP operations and extract results. This class executes LDAP operations, obtaining a DirContext,
 * freeing it and converting checked, LDAP specific exceptions into the exception hierarchy defined in the org.springframework.dao package.
 * <p>
 * Code using this class need only implement callbck interfaces, giving them a clearly defined contract. The OperationCallback is the most generic
 * LDAP operation callback, its main method is called with a DirContext already allocated and automatically freed. This method can perform any
 * querying, updates or deletion using the given DirContext. It is possible, and encouraged, to have a LdapTemplate associated with the specialized
 * DirContext so that the callback class don't have to care about navigating up to the data, but expect them to be at the root of the given
 * DirContext.
 * </p>
 * <p>
 * This class also uses the SearchResultCallbackHandler to wrap the search methods found in DirContext into a version which calls the main method of
 * the SearchResultCallbackHandler with the current SearchResult. The NamingEnumeration iterating over the result is automatically freed. The callback
 * can be stateful hence a getResult method allows to return a value which becomes the one of the search method.
 * </p>
 * <p>
 * A LdapTemplate is bound to a given LDAP url, either a server instance, an authentication and a relative root inside the directory. The
 * contextSource is the property which defined which url is linked to the LDAPTemplate. This contextSource provides a DirContext for each execution,
 * thus making a LdapTemplate reusable.
 * </p>
 * 
 * @author Olivier Jolly
 * @see javax.naming.directory.DirContext
 * @see javax.naming.NamingException
 * @see org.springframework.ldap.core.OperationCallback
 * @see javax.naming.directory.SearchResult
 * @see org.springframework.ldap.core.SearchResultCallbackHandler
 * @see javax.naming.directory.DirContext#search(java.lang.String, javax.naming.directory.Attributes)
 * @see javax.naming.directory.DirContext#search(java.lang.String, javax.naming.directory.Attributes, java.lang.String[])
 * @see javax.naming.directory.DirContext#search(java.lang.String, java.lang.String, java.lang.Object[], javax.naming.directory.SearchControls)
 * @see javax.naming.directory.DirContext#search(java.lang.String, java.lang.String, javax.naming.directory.SearchControls)
 */
public class LdapTemplate {

    private ContextSource contextSource;

    private LdapExceptionTranslator exceptionTranslator = null;

    /**
     * The basic no arg constructor
     */
    public LdapTemplate() {
        // default constructor
    }

    /**
     * The constructor with a given LDAP location via a contextSource
     * 
     * @param contextSource the DirContext provider implementation
     */
    public LdapTemplate(ContextSource contextSource) {
        this.contextSource = contextSource;
    }

    /**
     * @return Returns the contextSource.
     */
    public ContextSource getContextSource() {
        return this.contextSource;
    }

    /**
     * @param contextSource The contextSource to set.
     */
    public void setContextSource(ContextSource contextSource) {
        this.contextSource = contextSource;
    }

    /**
     * @return Returns the exceptionTranslator.
     */
    public LdapExceptionTranslator getExceptionTranslator() {
        // TODO : add better synchronisation for multithread
        if (this.exceptionTranslator == null) {
            this.exceptionTranslator = new CanonicalLdapExceptionTranslator();
        }
        return this.exceptionTranslator;
    }

    /**
     * @param exceptionTranslator The exceptionTranslator to set.
     */
    public void setExceptionTranslator(
        LdapExceptionTranslator exceptionTranslator) {
        this.exceptionTranslator = exceptionTranslator;
    }

    /*
     * Generic access method
     */

    /**
     * Most generic execution method which is configured with a callback to which we provide a DirContext instance from the contextSource. The
     * DirContext instance is then freed and LDAP exceptions, if any, are translated to the generic dao ones, using the exceptionTranslator instance.
     * 
     * @param action callback given the dirContext instance
     * @return the return value of the doInAction method.
     */
    public Object execute(final OperationCallback action) {
        DirContext context = getContextSource().getDirContext();
        try {
            Object result = action.doInAction(context);
            return result;
        }
        catch (NamingException ex) {
            throw getExceptionTranslator().translate(
                "executing OperationCallback", ex);
        }
        finally {
            LdapUtils.closeContext(context);
        }
    }

    /*
     * Access method for Context queries with callback handler
     */
    public Object search(final String name, final String filter,
        final SearchControls searchControls,
        final SearchResultCallbackHandler callback) {
        return this.execute(new OperationCallback(){

            public Object doInAction(DirContext context)
                throws NamingException, DataAccessException {
                return parseNamingEnumeration(context.search(name, filter,
                    searchControls), callback);
            }
        });
    }

    public Object search(final String name, final String filterExpr,
        final Object[] filterArgs, final SearchControls cons,
        final SearchResultCallbackHandler callback) {
        return this.execute(new OperationCallback(){

            public Object doInAction(DirContext context)
                throws NamingException, DataAccessException {
                return parseNamingEnumeration(context.search(name, filterExpr,
                    filterArgs, cons), callback);
            }
        });
    }

    public Object search(final String name,
        final Attributes matchingAttributes,
        final SearchResultCallbackHandler callback) {
        return search(name, matchingAttributes, null, callback);
    }

    public Object search(final String name,
        final Attributes matchingAttributes, final String[] attributesToReturn,
        final SearchResultCallbackHandler callback) {
        return this.execute(new OperationCallback(){

            public Object doInAction(DirContext context)
                throws NamingException, DataAccessException {
                return parseNamingEnumeration(context.search(name,
                    matchingAttributes, attributesToReturn), callback);
            }
        });
    }

    // TODO think about the need to add the search(Name...) wrappers

    /**
     * Internal factorised method which iterates over a NamingEnumeration, applying the SearchResultCallbackHandler method on each SearchResult and
     * finally free the enumeration itself.
     * 
     * @param namingEnumeration the enumeration to iterate through
     * @param callback the callback to apply on each result
     * @return the result value of the callback getResult() method
     * @throws NamingException raised for any LDAP problem
     */
    protected Object parseNamingEnumeration(
        NamingEnumeration namingEnumeration,
        SearchResultCallbackHandler callback) throws NamingException {
        try {
            while (namingEnumeration.hasMore()) {
                callback.processSearchResult((SearchResult)namingEnumeration
                    .next());
            }
            return callback.getResult();
        }
        finally {
            namingEnumeration.close();
        }
    }

}