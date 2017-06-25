package org.apereo.cas.support.spnego.util;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *  Utility class to perform DNS work in a threaded, timeout-able way
 *  Adapted from: http://thushw.blogspot.com/2009/11/resolving-domain-names-quickly-with.html.
 *
 *  @author Sean Baker sean.baker@usuhs.edu
 *  @author Misagh Moayyed
 *  @since 4.1
 */
public class ReverseDNSRunnable implements Runnable {

    /** Logger instance. **/
    private static final Logger LOGGER = LoggerFactory.getLogger(ReverseDNSRunnable.class);

    /** Remote user IP address. **/
    private final String ipAddress;

    /** Remote user hostname. **/
    private String hostName;

    /**
     * Simple constructor which also pre-sets hostName attribute for failover situations.
     * @param ipAddress the ip address on which reverse DNS will be done.
     */
    public ReverseDNSRunnable(final String ipAddress) {
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
            LOGGER.debug("Attempting to resolve [{}]", this.ipAddress);
            final InetAddress address = InetAddress.getByName(this.ipAddress);
            set(address.getCanonicalHostName());
        } catch (final UnknownHostException e) {
            /* N/A -- Default to IP address, but that's already done. **/
            LOGGER.debug("Unable to identify the canonical hostname for ip address.", e);
        }
    }

    /**
     * Glorified setter with logging.
     * @param hostName the resolved hostname
     */
    public synchronized void set(final String hostName) {
        LOGGER.trace("ReverseDNS -- Found hostName: [{}].", hostName);
        this.hostName = hostName;
    }

    /**
     * Getter method to buildIdentifier result of lookup.
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
