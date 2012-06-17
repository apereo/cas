/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */ 
package org.jasig.cas.adaptors.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;

/**
 * Authentication strategy for LDAP DIGEST-MD5.
 * 
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 4.0
 */
public class DigestMd5DirContextAuthenticationStrategy
    implements DirContextAuthenticationStrategy
{
    /** Authentication type for DIGEST-MD5 auth */
    private static final String DIGEST_MD5_AUTHENTICATION = "DIGEST-MD5";


    /** {@inheritDoc} */
    public DirContext processContextAfterCreation(
        final DirContext ctx,
        final String userDn,
        final String password)
        throws NamingException {

        return ctx;
    }


    /** {@inheritDoc} */
    @SuppressWarnings(value = "unchecked")
    public void setupEnvironment(
        final Hashtable env,
        final String userDn,
        final String password)
        throws NamingException {

        env.put(Context.SECURITY_AUTHENTICATION, DIGEST_MD5_AUTHENTICATION);
        // userDn should be a bare username for DIGEST-MD5
        env.put(Context.SECURITY_PRINCIPAL, userDn);
        env.put(Context.SECURITY_CREDENTIALS, password);

    }

}
