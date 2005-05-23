package org.jasig.cas.web.init;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * 
 * @author Andrew Petro
 * @version $Revision$ $Date$
 * @see DispatcherServlet
 */
public final class SafeDispatcherServlet extends HttpServlet {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = 1L;

    public static final String CAUGHT_THROWABLE_KEY = "exceptionCaughtByServlet";

    private Log log = LogFactory.getLog(getClass());

    private DispatcherServlet delegate = new DispatcherServlet();

    public void init(ServletConfig config) {
        try {
            this.delegate.init(config);

        } catch (Throwable t) {
            // no matter what went wrong, our role is to capture this error and
            // prevent it from blocking initialization of the servlet.

            // logging overkill so that our deployer will find a record of this
            // problem
            // even if unfamiliar with Commons Logging and properly configuring
            // it.

            final String message = "SafeDispatcherServlet: \n"
                + "The Spring DispatcherServlet we wrap threw on init.\n"
                + "But for our having caught this error, the servlet would not have initialized.";

            // log it via Commons Logging
            log.fatal(message, t);

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

    public void service(ServletRequest req, ServletResponse resp)
        throws ServletException, IOException {
        /*
         * Since our container calls only this method and not any of the other
         * HttpServlet runtime methods, such as doDelete(), etc., delegating
         * this method is sufficient to delegate all of the methods in the
         * HttpServlet API.
         */

        this.delegate.service(req, resp);
    }
}
