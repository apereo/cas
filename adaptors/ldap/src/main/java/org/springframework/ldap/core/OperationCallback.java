/*
 * Copyright 2002-2004 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package org.springframework.ldap.core;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.springframework.dao.DataAccessException;

/**
 * Generic callback interface for code that operates with LDAP directory.
 * 
 * @author Olivier Jolly
 * @see org.springframework.ldap.core.LdapTemplate
 */
public interface OperationCallback {

    /**
     * Get called by LdapTemplate.execute with an active DirContext. Do not need to care about getting an active context nor releasing it.
     * 
     * @param context active context
     * @return a return object, or null
     * @throws NamingException if thrown by a ldap operation, to be auto converted (TODO: with a customisable translater or fixed one ?)
     * @throws DataAccessException in case of custom exceptions
     */
    Object doInAction(DirContext context) throws NamingException,
        DataAccessException;

}