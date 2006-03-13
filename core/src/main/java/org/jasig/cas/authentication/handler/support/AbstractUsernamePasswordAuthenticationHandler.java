/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.PasswordEncoder;
import org.jasig.cas.authentication.handler.PlainTextPasswordEncoder;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract class to override supports so that we don't need to duplicate the
 * check for UsernamePasswordCredentials.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * <p>This is a published and supported CAS Server 3 API.</p>
 */
public abstract class AbstractUsernamePasswordAuthenticationHandler implements
    AuthenticationHandler, InitializingBean {

    /** Default class to support if one is not supplied. */
    private static final Class DEFAULT_CLASS = UsernamePasswordCredentials.class;

    /** Class that this instance will support. */
    private Class classToSupport;

    /**
     * Boolean to determine whether to support subclasses of the class to
     * support.
     */
    private boolean supportSubClasses = true;

    /**
     * PasswordEncoder to be used by subclasses to encode passwords for
     * comparing against a resource.
     */
    private PasswordEncoder passwordEncoder;

    /** Instance of logging for subclasses. */
    protected Log log = LogFactory.getLog(this.getClass());

    /**
     * Method automatically handles conversion to UsernamePasswordCredentials
     * and delegates to abstract authenticateUsernamePasswordInternal so
     * subclasses do not need to cast.
     */
    public final boolean authenticate(final Credentials credentials)
        throws AuthenticationException {
        return authenticateUsernamePasswordInternal((UsernamePasswordCredentials) credentials);
    }

    /**
     * Abstract convenience method that assumes the credentials passed in are a
     * subclass of UsernamePasswordCredentials.
     * 
     * @param credentials the credentials representing the Username and Password
     * presented to CAS
     * @return true if the credentials are authentic, false otherwise.
     * @throws AuthenticationException if authenticity cannot be determined.
     */
    protected abstract boolean authenticateUsernamePasswordInternal(
        final UsernamePasswordCredentials credentials)
        throws AuthenticationException;

    public final void afterPropertiesSet() throws Exception {
        if (this.passwordEncoder == null) {
            this.passwordEncoder = new PlainTextPasswordEncoder();
            getLog().info(
                "No PasswordEncoder set.  Using default: "
                    + this.passwordEncoder.getClass().getName());
        }

        if (this.classToSupport == null) {
            this.classToSupport = DEFAULT_CLASS;
            getLog().info(
                "No Class to Support set.  Using default: "
                    + this.classToSupport.getName());
        }
        afterPropertiesSetInternal();
    }

    /**
     * Method designed to be overwritten subclasses that need to do additional
     * properties checking.
     * 
     * @throws Exception if there is an error checking the properties.
     */
    protected void afterPropertiesSetInternal() throws Exception {
        // this is designed to be overwritten
    }

    /**
     * Method to return the PasswordEncoder to be used to encode passwords.
     * 
     * @return the PasswordEncoder associated with this class.
     */
    protected final PasswordEncoder getPasswordEncoder() {
        return this.passwordEncoder;
    }

    /**
     * Method to return the log instance in order for subclasses to have access
     * to the log object.
     * 
     * @return the logging instance for this class.
     */
    public final Log getLog() {
        return this.log;
    }

    /**
     * Method to set the class to support.
     * 
     * @param classToSupport the class we want this handler to support
     * explicitly.
     */
    public final void setClassToSupport(final Class classToSupport) {
        this.classToSupport = classToSupport;
    }

    /**
     * Method to set whether this handler will support subclasses of the
     * supported class.
     * 
     * @param supportSubClasses boolean of whether to support subclasses or not.
     */
    public final void setSupportSubClasses(final boolean supportSubClasses) {
        this.supportSubClasses = supportSubClasses;
    }

    /**
     * Sets the PasswordEncoder to be used with this class.
     * 
     * @param passwordEncoder the PasswordEncoder to use when encoding
     * passwords.
     */
    public final void setPasswordEncoder(final PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @return true if the credentials are not null and the credentials class is
     * equal to the class defined in classToSupport.
     */
    public final boolean supports(final Credentials credentials) {
        return credentials != null
            && (this.classToSupport.equals(credentials.getClass()) || (this.classToSupport
                .isAssignableFrom(credentials.getClass()))
                && this.supportSubClasses);
    }
}
