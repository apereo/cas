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
package org.jasig.cas.web.init;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;

/**
 * A {@link ServletContextListener} which wraps Spring's
 * {@link ContextLoaderListener} and catches anything that delegate throws so as
 * to prevent its having thrown a Throwable from aborting the initialization of
 * our entire web application context. Use of this ContextListener will not be
 * appropriate for all deployments of a web application. Sometimes, a context
 * listener's aborting context initialization is exactly the desired behavior.
 * This might be because the resulting application inavailability is acceptable
 * or because another layer (Apache, a proxy, a balancer, etc.) detects the
 * inavailability of the context and provides an appropriate user experience. Or
 * because a root context handles all requests that aren't otherwise handled.
 * There are many fine alternatives to this approach. However, when using the
 * bare Tomcat container with CAS as the root context, if your desired behavior
 * is that a failure at context initialization results in a dummy CAS context
 * that presents a user-friendly "CAS is unavailable at this time" message,
 * using this ContextListener in place of Spring's {@link ContextLoaderListener}
 * should do the trick. The error page associated with this deployment failure
 * is configured in the web.xml via the standard error handling mechanism.
 * Rather than being a generic ContextListener wrapper that will make safe any
 * ContextListener, instead this implementation specifically wraps and delegates
 * to a Spring {@link ContextLoaderListener}. As such, mapping this listener in
 * web.xml is a one for one replacement for {@link ContextLoaderListener}.
 * <p>
 * The exception thrown is exposed in the Servlet Context under the key
 * "exceptionCaughtByListener".
 * 
 * @author Andrew Petro
 * @version $Revision$ $Date$
 * @see ContextLoaderListener
 */
public final class SafeContextLoaderListener implements ServletContextListener {

    /** Instance of Commons Logging. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The name of the ServletContext attribute whereat we will place a List of
     * Throwables that we caught from our delegate context listeners.
     */
    public static final String CAUGHT_THROWABLE_KEY = "exceptionCaughtByListener";

    /** The actual ContextLoaderListener to which we will delegate to. */
    private final ContextLoaderListener delegate = new ContextLoaderListener();

    public void contextInitialized(final ServletContextEvent sce) {
        try {
            this.delegate.contextInitialized(sce);
        } catch (Throwable t) {
            /*
             * no matter what went wrong, our role is to capture this error and
             * prevent it from blocking initialization of the context. logging
             * overkill so that our deployer will find a record of this problem
             * even if unfamiliar with Commons Logging and properly configuring
             * it.
             */

            final String message = "SafeContextLoaderListener: \n"
                + "The Spring ContextLoaderListener we wrap threw on contextInitialized.\n"
                + "But for our having caught this error, the web application context would not have initialized.";

            // log it via Commons Logging
            log.error(message, t);

            // log it to System.err
            System.err.println(message);
            t.printStackTrace();

            // log it to the ServletContext
            ServletContext context = sce.getServletContext();
            context.log(message, t);

            /*
             * record the error so that the application has access to later
             * display a proper error message based on the exception.
             */
            context.setAttribute(CAUGHT_THROWABLE_KEY, t);
        }
    }

    public void contextDestroyed(final ServletContextEvent sce) {
        this.delegate.contextDestroyed(sce);
    }

}
