/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * An implementation of {@link UniqueTicketIdGenerator} that is able auto-configure
 * the suffix based on the underlying host name.
 *
 * <p>In order to assist with multi-node deployments, in scenarios where CAS configuration
 * and specially <code>cas.properties</code> file is externalized, it would be ideal to simply just have one set
 * of configuration files for all nodes, such that there would for instance be one <code>cas.properties</code> file
 * for all nodes. This would remove the need to copy/sync config files over across nodes, again in a
 * situation where they are externalized.
 * <p>The drawback is that in keeping only one <code>cas.properties</code> file, we'd lose the ability
 * to define unique <code>host.name</code> property values for each node as the suffix, which would assist with troubleshooting
 * and diagnostics. To provide a remedy, this ticket generator is able to retrieve the <code>host.name</code> value directly from
 * the actual node name, rather than relying on the configuration, only if one isn't specified in
 * the <code>cas.properties</code> file. </p>
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class HostNameBasedUniqueTicketIdGenerator extends DefaultUniqueTicketIdGenerator {
    /**
     * Instantiates a new Host name based unique ticket id generator.
     *
     * @param maxLength the max length
     */
    public HostNameBasedUniqueTicketIdGenerator(final int maxLength) {
        super(maxLength, determineTicketSuffixByHostName());
    }

    /**
     * Appends the first part of the host name to the ticket id,
     * so as to moderately provide a relevant unique value mapped to
     * the host name AND not auto-leak infrastructure data out into the configuration and logs.
     * <ul>
     * <li>If the CAS node name is <code>cas-01.sso.edu</code> then, the suffix
     * determined would just be <code>cas-01</code></li>
     * <li>If the CAS node name is <code>cas-01</code> then, the suffix
     * determined would just be <code>cas-01</code></li>
     * </ul>
     * @return the shortened ticket suffix based on the hostname
     * @since 4.1.0
     */
    private static String determineTicketSuffixByHostName() {
        try {
            final String hostName = InetAddress.getLocalHost().getCanonicalHostName();
            final int index = hostName.indexOf('.');
            if (index > 0) {
                return hostName.substring(0, index);
            }
            return hostName;
        } catch (final UnknownHostException e) {
            throw new RuntimeException("Host name could not be determined automatically for the ticket suffix.", e);
        }
    }
}
