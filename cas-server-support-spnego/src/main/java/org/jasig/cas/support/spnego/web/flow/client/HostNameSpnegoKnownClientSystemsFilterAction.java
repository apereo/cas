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

package org.jasig.cas.support.spnego.web.flow.client;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * A simple implementation of {@link BaseSpnegoKnownClientSystemsFilterAction} to allow / skip SPNEGO / KRB /
 * NTLM authentication based on a regex match against a reverse DNS lookup of the requesting
 * system.
 *
 * @author Sean Baker
 * @author Misagh Moayyed
 * @since 4.1
 */
public class HostNameSpnegoKnownClientSystemsFilterAction extends BaseSpnegoKnownClientSystemsFilterAction {
    private static final int DEFAULT_TIMEOUT = 5000;

    /** Timeout for DNS Requests. **/
    private long timeout = DEFAULT_TIMEOUT;

    private final Pattern hostNamePatternString;

    /**
     * Instantiates a new hostname spnego known client systems filter action.
     *
     * @param hostNamePatternString the host name pattern string.
     *                              The pattern to match the retrieved hostname against.
     */
    public HostNameSpnegoKnownClientSystemsFilterAction(@NotNull final String hostNamePatternString) {
        super();
        this.hostNamePatternString = Pattern.compile(hostNamePatternString);
    }

    /**
     * {@inheritDoc}. <p/>
     * Checks whether the IP should even be paid attention to,
     * then does a reverse DNS lookup, and if it matches the supplied pattern, performs SPNEGO
     * else skips the process.
     */
    @Override
    protected boolean shouldDoSpnego() {
        final String hostName = getRemoteHostName();
        logger.debug("Retrieved host name for the remote ip is {}", hostName);
        return this.hostNamePatternString.matcher(hostName).find();
    }


    /**
     * Set timeout (ms) for DNS requests; valuable for heterogeneous environments employing
     * fall-through authentication mechanisms.
     * @param timeout # of milliseconds to wait for a DNS request to return
     */
    public final void setTimeout(final long timeout) {
        this.timeout = timeout;
    }

    /**
     * Convenience method to perform a reverse DNS lookup. Threads the request
     * through a custom Runnable class in order to prevent inordinately long
     * user waits while performing reverse lookup.
     * @return the remote host name
     */
    private String getRemoteHostName() {
        final ReverseDNS revDNS = new ReverseDNS(getRemoteIp());

        final Thread t = new Thread(revDNS);
        t.start();

        try {
            t.join(this.timeout);
        } catch (final InterruptedException e) {
            logger.debug("Threaded lookup failed.  Defaulting to IP {}.", getRemoteIp(), e);
        }

        final String remoteHostName = revDNS.get();
        logger.debug("Found remote host name {}.", remoteHostName);

        return StringUtils.isNotEmpty(remoteHostName) ? remoteHostName : getRemoteIp();
    }


    /**
     *  Utility class to perform DNS work in a threaded, timeout-able way
     *  Adapted from: http://thushw.blogspot.com/2009/11/resolving-domain-names-quickly-with.html.
     *
     *  @author Sean Baker sean.baker@usuhs.edu
     *  @author Misagh Moayyed
     *  @since 4.1
     */
    private static final class ReverseDNS implements Runnable {

        /** Logger instance. **/
        private static final Logger LOGGER = LoggerFactory.getLogger(ReverseDNS.class);

        /** Remote user IP address. **/
        private final String ipAddress;

        /** Remote user hostname. **/
        private String hostName;

        /**
         * Simple constructor which also pre-sets hostName attribute for failover situations.
         * @param ipAddress the ip address on which reverse DNS will be done.
         */
        public ReverseDNS(final String ipAddress) {
            this.ipAddress = ipAddress;
            this.hostName = ipAddress;
        }

        /**
         * Runnable implementation to thread the work done in this class, allowing the
         * implementer to set a thread timeout and thereby short-circuit the lookup.
         */
        @Override
        public void run() {
            try {
                LOGGER.debug("Attempting to resolve {}", this.ipAddress);
                final InetAddress address = InetAddress.getByName(this.ipAddress);
                set(address.getCanonicalHostName());
            } catch (final UnknownHostException e) {
                /** N/A -- Default to IP address, but that's already done. **/
                LOGGER.debug("Unable to identify the canonical hostname for ip address.", e);
            }
        }

        /**
         * Glorified setter with logging.
         * @param hostName the resolved hostname
         */
        public synchronized void set(final String hostName) {
            LOGGER.trace("ReverseDNS -- Found hostName: {}.", hostName);
            this.hostName = hostName;
        }

        /**
         * Getter method to provide result of lookup.
         * @return the remote host name, or the IP address if name not found
         */
        public synchronized String get() {
            return this.hostName;
        }


        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("ipAddress", this.ipAddress)
                    .append("hostName", this.hostName)
                    .toString();
        }
    }
}
