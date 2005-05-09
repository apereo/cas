package org.jasig.cas.web.init;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ContextLoaderListener;

/**
 * A {@link ServletContextListener} which wraps 
 * Spring's {@link ContextLoaderListener} and catches 
 * anything that delegate throws so as to prevent its having thrown a Throwable
 * from aborting the initialization of our entire web application context.
 * 
 * Use of this ContextListener will not be appropriate for all deployments of a web
 * application.  Sometimes, a context listener's aborting context initialization is 
 * exactly the desired behavior. This might be because the resulting application
 * inavailability is acceptable or because another layer (Apache, a proxy, a balancer,
 * etc.) detects the inavailability of the context and provides an appropriate user
 * experience. Or because a root context handles all requests that aren't otherwise
 * handled.  There are many fine alternatives to this approach.
 * 
 * However, when using the bare Tomcat container with CAS as the root context,
 * if your desired behavior is that a failure at context initialization results in a dummy
 * CAS context that presents a user-friendly "CAS is unavailable at this time" message,
 * using this ContextListener in place of Spring's {@link ContextLoaderListener} 
 * should do the trick.
 * 
 * Using this context listener only makes sense when you also map the
 * {@link ContextInitFailureFilter} to provide a reasonable user experience for the
 * (broken) web application that this ContextListener allows to initialize.  You must also
 * provide an appropriate JSP to which {@link ContextInitFailureFilter} can forward.
 * 
 * Rather than being a generic ContextListener wrapper that will make safe any
 * ContextListener, instead this implementation specifically wraps and delegates to
 * a Spring {@link ContextLoaderListener}.  As such, mapping this listener in web.xml is
 * a one for one replacement for {@link ContextLoaderListener}.
 * 
 * @version $Revision$ $Date$
 * @see ContextLoaderListener
 * @see ContextInitFailureFilter
 */
public final class SafeContextLoaderListener 
    implements ServletContextListener {
    
    protected final Log log = LogFactory.getLog(getClass());
    
    /**
     * The name of the ServletContext attribute whereat we will place a List of
     * Throwables that we caught from our delegate context listeners.
     */
    public static final String CAUGHT_THROWABLE_KEY = "org.jasig.cas.web.init.SafeDelegatingContextListener.CAUGHT";

    private final ContextLoaderListener delegate = new ContextLoaderListener();
    
    public void contextInitialized(ServletContextEvent sce) {
        try {
            this.delegate.contextInitialized(sce);
        } catch (Throwable t) {
            // no matter what went wrong, our role is to capture this error and prevent
            // it from blocking initialization of the context.
            
            // logging overkill so that our deployer will find a record of this problem
            // even if unfamiliar with Commons Logging and properly configuring it.
            
            final String message = "SafeContextLoaderListener: \n" +
                    "The Spring ContextLoaderListener we wrap threw on contextInitialized.\n" +
            "But for our having caught this error, the web application context would not have initialized.";
            
            // log it via Commons Logging
            log.fatal(message, t);
            
            // log it to System.err
            System.err.println(message);
            t.printStackTrace();
            
            // log it to the ServletContext
            ServletContext context = sce.getServletContext();
            context.log(message, t);
            
            
            // record the error so that the ContextListenerFailureFilter can detect the error condition
            // make the throwable available to the eventual error UI
            
            context.setAttribute(CAUGHT_THROWABLE_KEY, t);
            
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        this.delegate.contextDestroyed(sce);
    }
    
}

