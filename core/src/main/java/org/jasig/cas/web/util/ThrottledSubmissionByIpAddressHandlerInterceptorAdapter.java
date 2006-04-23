/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.util;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Implementation of a HandlerInterceptorAdapter that keeps track of a mapping
 * of IP Addresses to number of failures to authenticate.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
public final class ThrottledSubmissionByIpAddressHandlerInterceptorAdapter
    extends HandlerInterceptorAdapter implements InitializingBean {

    /** Default value for the failure threshhold before you're locked out. */
    private static final BigInteger DEFAULT_FAILURE_THRESHHOLD = BigInteger.valueOf(100);

    /** The default timeout (in seconds) to clear one failure attempt. */
    private static final int DEFAULT_FAILURE_TIMEOUT = 60;
    
    /** Cache of the starting Integer. */
    protected static final BigInteger ONE = BigInteger.valueOf(1);
    
    /** The map of restricted IPs mapped to failures. */
    private Map restrictedIpAddresses = new HashMap();

    /** The threshhold before we stop someone from authenticating. */
    private BigInteger failureThreshhold = DEFAULT_FAILURE_THRESHHOLD;

    /** The failure timeout before we clean up one failure attempt. */
    int failureTimeout = DEFAULT_FAILURE_TIMEOUT;

    public void postHandle(final HttpServletRequest request,
        final HttpServletResponse response, final Object handler,
        final ModelAndView modelAndView) throws Exception {
        if (request.getMethod().equals("GET")
            || !modelAndView.getViewName().equals("casLoginView")) {
            return;
        }

        // XXX can we synchronize on IP Address?
        synchronized (this.restrictedIpAddresses) {
            final String remoteIpAddress = request.getRemoteAddr();
            final BigInteger original = (BigInteger) this.restrictedIpAddresses
                .get(remoteIpAddress);
            BigInteger integer = ONE;

            if (original != null) {
                integer = original.add(ONE);
            }

            this.restrictedIpAddresses.put(remoteIpAddress, integer);

            if (integer.compareTo(this.failureThreshhold) == 1) {
                modelAndView.setViewName("casFailureAuthenticationThreshhold");
            }
        }
    }

    public void setFailureThreshhold(final BigInteger failureThreshhold) {
        this.failureThreshhold = failureThreshhold;
    }

    /**
     * Set the timeout for failure in seconds.
     * 
     * @param failureTimeout the failure timeout
     */
    public void setFailureTimeout(final int failureTimeout) {
        this.failureTimeout = failureTimeout;
    }

    public void afterPropertiesSet() throws Exception {
        final Thread thread = new ExpirationThread(this.restrictedIpAddresses,
            this.failureTimeout);
        thread.setDaemon(true);
        thread.start();
    }

    protected final class ExpirationThread extends Thread {

        /** Reference to the map of restricted IP addresses. */
        private Map restrictedIpAddresses;

        /** The timeout failure. */
        private int failureTimeout;

        public ExpirationThread(final Map restrictedIpAdddresses,
            final int failureTimeout) {
            this.restrictedIpAddresses = restrictedIpAdddresses;
            this.failureTimeout = failureTimeout;
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(this.failureTimeout * 60);
                    cleanUpFailures();
                } catch (final InterruptedException e) {
                    // nothing to do
                }
            }
        }

        private void cleanUpFailures() {
            final Set keys = this.restrictedIpAddresses.keySet();

            synchronized (this.restrictedIpAddresses) {
                for (final Iterator iter = keys.iterator(); iter.hasNext();) {
                    final Object key = iter.next();
                    final BigInteger integer = (BigInteger) this.restrictedIpAddresses
                        .get(key);
                    final BigInteger newValue = integer.subtract(ONE);

                    if (newValue.equals(BigInteger.ZERO)) {
                        this.restrictedIpAddresses.remove(key);
                    } else {
                        this.restrictedIpAddresses.put(key, newValue);
                    }
                }
            }
        }
    }
}
