/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.support;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Implementation of a HandlerInterceptorAdapter that keeps track of a mapping
 * of IP Addresses to number of failures to authenticate.
 * <p>
 * Implementation attempts to optimize access by using the last quad in an IP
 * address as a form of poor man's lock.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
public final class ThrottledSubmissionByIpAddressHandlerInterceptorAdapter
    extends HandlerInterceptorAdapter implements InitializingBean {

    /** Default value for the failure threshhold before you're locked out. */
    private static final int DEFAULT_FAILURE_THRESHHOLD = 100;

    /** The default timeout (in seconds) to clear one failure attempt. */
    private static final int DEFAULT_FAILURE_TIMEOUT = 60;

    /** Cache of the starting Integer. */
    protected static final BigInteger ONE = BigInteger.valueOf(1);

    private final Log log = LogFactory.getLog(getClass());
    
    private ConcurrentMap<String, AtomicInteger> ipMap = new ConcurrentHashMap<String, AtomicInteger>();

    /** The threshold before we stop someone from authenticating. */
    private int failureThreshhold = DEFAULT_FAILURE_THRESHHOLD;
    
    /** The failure timeout before we clean up one failure attempt. */
    int failureTimeout = DEFAULT_FAILURE_TIMEOUT;

    public void postHandle(final HttpServletRequest request,
        final HttpServletResponse response, final Object handler,
        final ModelAndView modelAndView) throws Exception {
        if (!request.getMethod().equals("GET")
            || !"casLoginView".equals(modelAndView.getViewName())) {
            return;
        }
        final String remoteAddr = request.getRemoteAddr();
        final AtomicInteger newInteger = new AtomicInteger(0);
        final AtomicInteger currentInteger = this.ipMap.putIfAbsent(remoteAddr, newInteger);
        final AtomicInteger atomicInteger;

        if (currentInteger != null) {
            atomicInteger = currentInteger;
        } else {
            atomicInteger = newInteger;
        }
        
        atomicInteger.incrementAndGet();
              
        if (atomicInteger.intValue() >= this.failureThreshhold) {
            log.warn("Possible hacking attack from " + remoteAddr + ". More than " + this.failureThreshhold + " failed login attempts within " + this.failureTimeout + " seconds.");
            modelAndView.setViewName("casFailureAuthenticationThreshhold");
        }
    }

    public void setFailureThreshhold(final int failureThreshhold) {
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

        final Thread thread = new ExpirationThread(
            this.ipMap, this.failureTimeout);
        thread.setDaemon(true);
        thread.start();
    }

    protected final static class ExpirationThread extends Thread {

        /** Reference to the map of restricted IP addresses. */
        private ConcurrentMap<String, AtomicInteger> ipMap;

        /** The timeout failure. */
        private int failureTimeout;

        public ExpirationThread(
            final ConcurrentMap<String, AtomicInteger> ipMap,
            final int failureTimeout) {
            this.ipMap = ipMap;
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
            final Set<String> keys = this.ipMap.keySet();
            
            for (final Iterator<String> iter = keys.iterator(); iter.hasNext();) {
                final String key = iter.next();
                final AtomicInteger integer = this.ipMap.get(key);
                final int  newValue = integer.decrementAndGet();
                
                if (newValue == 0) {
                    iter.remove();
                }
            }
         }
    }
}
