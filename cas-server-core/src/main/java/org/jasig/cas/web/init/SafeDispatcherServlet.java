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

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * This servlet wraps the Spring DispatchServlet, catching any exceptions it
 * throws on init() to guarantee that servlet initialization succeeds. This
 * allows our application context to succeed in initializing so that we can
 * display a friendly "CAS is not available" page to the deployer (an
 * appropriate use of the page in development) or to the end user (an
 * appropriate use of the page in production). The error page associated with
 * this deployment failure is configured in the web.xml via the standard error
 * handling mechanism.
 * <p>
 * If the underlying Spring DispatcherServlet failed to init(), this
 * SafeDispatcherServlet will throw an
 * <code>org.springframework.context.ApplicationContextException</code> on
 * <code>service()</code>.
 * <p>
 * The exception thrown by the underlying Spring DispatcherServlet init() and
 * caught in the SafeDispatcherServlet init() is exposed as a Servlet Context
 * attribute under the key "exceptionCaughtByServlet".
 * 
 * @author Andrew Petro
 * @version $Revision$ $Date$
 * @see DispatcherServlet
 */
public final class SafeDispatcherServlet extends HttpServlet {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = 1L;

    /** Key under which we will store the exception in the ServletContext. */
    public static final String CAUGHT_THROWABLE_KEY = "exceptionCaughtByServlet";

    /** Instance of Commons Logging. */
    private static final Logger log = LoggerFactory.getLogger(SafeDispatcherServlet.class);

    /** The actual DispatcherServlet to which we will delegate to. */
    private DispatcherServlet delegate = new DispatcherServlet();

    /** Boolean to determine if the application deployed successfully. */
    private boolean initSuccess = true;

    public void init(final ServletConfig config) {
        try {
            this.delegate.init(config);

        } catch (final Throwable t) {
            // let the service method know initialization failed.
            this.initSuccess = false;

            /*
             * no matter what went wrong, our role is to capture this error and
             * prevent it from blocking initialization of the servlet. logging
             * overkill so that our deployer will find a record of this problem
             * even if unfamiliar with Commons Logging and properly configuring
             * it.
             */

            final String message = "SafeDispatcherServlet: \n"
                + "The Spring DispatcherServlet we wrap threw on init.\n"
                + "But for our having caught this error, the servlet would not have initialized.";

            // log it via Commons Logging
            log.error(message, t);

            // log it to System.err
            System.err.println(message);
            t.printStackTrace();

            // log it to the ServletContext
            ServletContext context = config.getServletContext();
            context.log(message, t);

            /*
             * record the error so that the application has access to later
             * display a proper error message based on the exception.
             */
            context.setAttribute(CAUGHT_THROWABLE_KEY, t);

        }
    }

    /**
     * @throws ApplicationContextException if the DispatcherServlet does not
     * initialize properly, but the servlet attempts to process a request.
     */
    public void service(final ServletRequest req, final ServletResponse resp)
        throws ServletException, IOException {
        /*
         * Since our container calls only this method and not any of the other
         * HttpServlet runtime methods, such as doDelete(), etc., delegating
         * this method is sufficient to delegate all of the methods in the
         * HttpServlet API.
         */
        if (this.initSuccess) {
            this.delegate.service(req, resp);
        } else {
            throw new ApplicationContextException(
                "Unable to initialize application context.");
        }
    }
}
