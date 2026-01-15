package org.apereo.cas.support.spnego.util;

import module java.base;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Utility class to perform DNS work in a threaded, timeout-able way
 * Adapted from: <a href="http://thushw.blogspot.com/2009/11/resolving-domain-names-quickly-with.html">here</a>.
 *
 * @author Sean Baker sean.baker@usuhs.edu
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
@ToString
@Getter
public class ReverseDNSRunnable implements Runnable {

    /**
     * Remote user IP address.
     **/
    private final String ipAddress;

    /**
     * Remote user hostname.
     **/
    @Setter
    private String hostName;

    /**
     * Simple constructor which also pre-sets hostName attribute for failover situations.
     *
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
            val address = InetAddress.getByName(this.ipAddress);
            setHostName(address.getCanonicalHostName());
        } catch (final UnknownHostException e) {
            /* N/A -- Default to IP address, but that's already done. **/
            LOGGER.debug("Unable to identify the canonical hostname for ip address.", e);
        }
    }
}
