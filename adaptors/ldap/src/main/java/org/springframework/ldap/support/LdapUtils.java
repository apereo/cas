/*
 * Copyright 2002-2004 the original author or authors. Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.springframework.ldap.support;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class factorising LDAP common operations
 * 
 * @author Olivier Jolly
 */
public class LdapUtils {

    private static final Log logger = LogFactory.getLog(LdapUtils.class);

    /**
     * Close the given context and ignore any thrown exception. This is useful
     * for typical finally blocks in manual Ldap statements.
     * 
     * @param context the Ldap context to close
     */
    public static void closeContext(DirContext context) {
        if (context != null) {
            try {
                context.close();
            }
            catch (NamingException ex) {
                logger.warn("Could not close context", ex);
            }
        }
    }

}