/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ldap.core;

import javax.naming.NamingException;
import javax.naming.directory.SearchResult;

/**
 * Callback interface used by LdapTemplate methods. The given object is a
 * Context, which is either a "root" for querying further and also a ldap object
 * whose attributes can be seen as sub contexts or retrieved more classicaly
 * with <code>getAttributes("")</code>
 * <p>
 * A ContextCallbackHandler is generally stateful : It keeps the result state
 * within the object, and provides it via the getResult method. If you do not
 * want to deal with the result method, you have the
 * SearchResultCallbackHandlerSupport abstract class which provide a default
 * implementation resulting null.
 * </p>
 * 
 * @author Olivier Jolly
 * @see org.springframework.ldap.core.SearchResultCallbackHandlerSupport
 */
public interface SearchResultCallbackHandler {

    /**
     * Implementations must implement this method to process each element of a
     * NamingEnumeration. The implementation doesn't have to care about
     * iterating in the enumeration.
     * 
     * @param searchResult
     *            the current item returned by the query
     * @throws NamingException
     *             if a NamingException is thrown during the processing
     */
    void processSearchResult(SearchResult searchResult) throws NamingException;

    /**
     * Implementation must provide a result value to be used at the end of the
     * iteration for returning to the caller of LdapTemplate
     * 
     * @return a return value
     */
    Object getResult();
}