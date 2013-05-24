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
package org.jasig.cas.util;

import org.ldaptive.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities related to LDAP functions.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public final class LdapUtils {

    private static final Logger log = LoggerFactory.getLogger(LdapUtils.class);

    private LdapUtils() {
        // private constructor so that no one can instantiate.
    }

    /**
     * Close the given context and ignore any thrown exception. This is useful
     * for typical finally blocks in manual Ldap statements.
     *
     * @param context the Ldap connection to close
     */
    public static void closeConnection(final Connection context) {
        if (context != null) {
            try {
                context.close();
            } catch (final Exception ex) {
                log.warn("Could not close ldap connection", ex);
            }
        }
    }
}
