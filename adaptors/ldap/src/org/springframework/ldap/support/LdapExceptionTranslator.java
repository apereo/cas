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
package org.springframework.ldap.support;

import javax.naming.NamingException;

import org.springframework.dao.DataAccessException;

/**
 * Interface to be implemented by classes that can translate between
 * NamingException and our data access trategy-agnotic
 * org.springframework.dao.DataAccessException.
 * 
 * @author Olivier Jolly
 * @see org.springframework.dao.DataAccessException
 */
public interface LdapExceptionTranslator {

    /**
     * Translate the given Ldap exception into a generic data access exception
     * 
     * @param task
     *            readable text describing the task being attempted
     * @param namingex
     *            NamingException encountered by Ldap implementation
     * @return the generic exception representing the ldap specific exception
     */
    DataAccessException translate(String task, NamingException namingex);

}