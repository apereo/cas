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
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract class for defining a simple binary filter to determine whether a
 * given client system should be prompted for SPNEGO / KRB / NTLM credentials.
 *
 * Envisioned implementations would include LDAP and DNS based determinations,
 * but of course others may have value as well for local architectures.
 *
 * @author Sean Baker sean.baker@usuhs.edu
 * @author Misagh Moayyed
 * @since 4.1
 */
public class BaseSpnegoKnownClientSystemsFilterAction extends AbstractAction {
    private static final int DEFAULT_TIMEOUT = 5000;
    private static final int THREAD_POOL_SIZE = 10;

    /** Logger instance. **/
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Pattern of ip addresses to check. **/
    private Pattern ipsToCheckPattern;

    /** Alternative remote host attribute. **/
    private String alternativeRemoteHostAttribute;

    /** The remote ip address. **/
    private String remoteIp;

    /** Timeout for DNS Requests. **/
    private long timeout = DEFAULT_TIMEOUT;

    private final ExecutorService executor;

    /**
     * Instantiation. Defaults the executor a service
     * to a fixed thread pool of size {@link #THREAD_POOL_SIZE}.
     */
    public BaseSpnegoKnownClientSystemsFilterAction() {
        this(Executors.newFixedThreadPool(THREAD_POOL_SIZE));
    }

    /**
     * Initializes the executor service.
     * @param executor the executor
     */
    public BaseSpnegoKnownClientSystemsFilterAction(final ExecutorService executor) {
        this(null, null, DEFAULT_TIMEOUT, executor);
    }

    /**
     * Instantiates a new Base.
     *
     * @param ipsToCheckPattern the ips to check pattern
     * @param alternativeRemoteHostAttribute the alternative remote host attribute
     * @param timeout the timeout
     */
    public BaseSpnegoKnownClientSystemsFilterAction(final String ipsToCheckPattern,
                                                    final String alternativeRemoteHostAttribute,
                                                    final long timeout) {
        this();
        setIpsToCheckPattern(ipsToCheckPattern);
        this.alternativeRemoteHostAttribute = alternativeRemoteHostAttribute;
        this.timeout = timeout;
    }

    /**
     * Instantiates a new Base.
     *
     * @param ipsToCheckPattern the ips to check pattern
     * @param alternativeRemoteHostAttribute the alternative remote host attribute
     */
    public BaseSpnegoKnownClientSystemsFilterAction(final String ipsToCheckPattern,
                                                    final String alternativeRemoteHostAttribute) {
        this(ipsToCheckPattern, alternativeRemoteHostAttribute, DEFAULT_TIMEOUT);
    }

    /**
     * Instantiates a new Base.
     *
     * @param ipsToCheckPattern the ips to check pattern
     */
    public BaseSpnegoKnownClientSystemsFilterAction(final String ipsToCheckPattern) {
        this(ipsToCheckPattern, null, DEFAULT_TIMEOUT);
    }

    /**
     * Instantiates a new Base.
     *
     * @param ipsToCheckPattern the ips to check pattern
     * @param alternativeRemoteHostAttribute the alternative remote host attribute
     * @param timeout the timeout
     * @param executor the executor
     */
    public BaseSpnegoKnownClientSystemsFilterAction(final Pattern ipsToCheckPattern,
                                                    final String alternativeRemoteHostAttribute,
                                                    final long timeout,
                                                    final ExecutorService executor) {
        this.ipsToCheckPattern = ipsToCheckPattern;
        this.alternativeRemoteHostAttribute = alternativeRemoteHostAttribute;
        this.timeout = timeout;
        this.executor = executor;
    }

    /**
     * {@inheritDoc}
     * Gets the remote ip from the request, and invokes spnego if it isn't filtered.
     *
     * @param context
     * @return {@link #yes()} if spnego should be invoked and ip isn't filtered,
     * {@link #no()} otherwise.
     */
    @Override
    protected final Event doExecute(@NotNull final RequestContext context) {
        this.remoteIp = getRemoteIp(context);
        logger.debug("Current user IP {}", this.remoteIp);
        return shouldCheckIP() && doSpnego() ? yes() : no();
    }

    /**
     * Simple pattern match to determine whether an IP should be checked.
     * Could stand to be extended to support "real" IP addresses and patterns, but
     * for the local / first implementation regex made more sense.
     * @return whether the remote ip received should be ignored
     */
    private boolean shouldCheckIP() {
        if (this.ipsToCheckPattern != null && StringUtils.isNotBlank(this.remoteIp)) {
            final Matcher matcher = this.ipsToCheckPattern.matcher(this.remoteIp);
            if (matcher.find()) {
                logger.debug("Remote IP address {} should be checked based on the defined pattern {}",
                        this.remoteIp, this.ipsToCheckPattern.pattern());
                return true;
            }
        }
        logger.debug("No pattern or remote IP defined, or pattern does not match remote IP [{}]",
                this.remoteIp);
        return false;
    }

    /**
     * Pulls the remote IP from the current HttpServletRequest, or grabs the value
     * for the specified alternative attribute (say, for proxied requests).  Falls
     * back to providing the "normal" remote address if no value can be retrieved
     * from the specified alternative header value.
     * @param context the context
     * @return the remote ip
     */
    private String getRemoteIp(@NotNull final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        String userAddress = request.getRemoteAddr();
        logger.debug("Remote Address = {}", userAddress);

        if (StringUtils.isNotBlank(this.alternativeRemoteHostAttribute)) {

            userAddress = request.getHeader(this.alternativeRemoteHostAttribute);
            logger.debug("Header Attribute [{}] = [{}]", this.alternativeRemoteHostAttribute, userAddress);

            if (StringUtils.isBlank(userAddress)) {
                userAddress = request.getRemoteAddr();
                logger.warn("No value could be retrieved from the header [{}]. Falling back to [{}].",
                        this.alternativeRemoteHostAttribute, userAddress);
            }
        }
        return userAddress;
    }

    /**
     * Destroy and shut down the executor.
     */
    @PreDestroy
    public final void destroy() {
        logger.debug("Shutting down the executor service");
        this.executor.shutdown();
    }
    /**
     * Convenience method to perform a reverse DNS lookup. Threads the request
     * through a custom Runnable class in order to prevent inordinately long
     * user waits while performing reverse lookup.
     * @return the remote host name
     */
    protected final String getRemoteHostName() {
        final ReverseDNS revDNS = new ReverseDNS(this.remoteIp);
        this.executor.submit(revDNS);

        final Thread t = new Thread(revDNS);
        t.start();

        try {
            t.join(this.timeout);
        } catch (final InterruptedException e) {
            logger.debug("Threaded lookup failed.  Defaulting to IP {}.", this.remoteIp, e);
        }

        final String remoteHostName = revDNS.get();
        logger.debug("Found remote host name {}.", remoteHostName);

        return StringUtils.isNotEmpty(remoteHostName) ? remoteHostName : this.remoteIp;
    }

    /**
     * Alternative header to be used for retrieving the remote system IP address.
     * @param alternativeRemoteHostAttribute the alternative remote host attribute
     */
    public final void setAlternativeRemoteHostAttribute(@NotNull final String alternativeRemoteHostAttribute) {
        this.alternativeRemoteHostAttribute = alternativeRemoteHostAttribute;
    }

    /**
     * Regular expression string to define IPs which should be considered.
     * @param ipsToCheckPattern the ips to check as a regex pattern
     */
    public final void setIpsToCheckPattern(@NotNull final String ipsToCheckPattern) {
        this.ipsToCheckPattern = Pattern.compile(ipsToCheckPattern);
    }

    /**
     * Set timeout (ms) for DNS requests; valuable for heterogeneous environments employing
     * fall-through authentication mechanisms.
     * @param timeout # of milliseconds to wait for a DNS request to return
     */
    public final void setTimeout(final long timeout) {
        this.timeout = timeout;
    }

    protected final String getRemoteIp() {
        return this.remoteIp;
    }

    @Override
    public final String toString() {
        return new ToStringBuilder(this)
                .append("ipsToCheckPattern", this.ipsToCheckPattern)
                .append("alternativeRemoteHostAttribute", this.alternativeRemoteHostAttribute)
                .append("remoteIp", this.remoteIp)
                .append("timeout", this.timeout)
                .append("executor", this.executor.getClass().getName())
                .toString();
    }

    /**
     * Left to the implementer to provide a binary value
     * as to whether Spnego should
     * be performed for the requesting system.
     * @return always true
     */
    protected boolean doSpnego() {
        return true;
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
