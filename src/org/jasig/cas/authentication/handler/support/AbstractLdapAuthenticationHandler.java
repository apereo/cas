/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import java.util.Hashtable;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public abstract class AbstractLdapAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private static final String DEFAULT_FILTER = "uid=%u";

    private static final String DEFAULT_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

    private static final String DEFAULT_AUTHENTICATION = "simple";

    private static final String DEFAULT_SECURITY_PROTOCOL = "ssl";

    private String authentication = DEFAULT_AUTHENTICATION;

    private String contextFactory = DEFAULT_CONTEXT_FACTORY;

    private String securityProtocol = DEFAULT_SECURITY_PROTOCOL;

    private String filter = DEFAULT_FILTER;

    private boolean secure;

    private List servers;

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if (this.servers == null || this.servers.isEmpty() || this.authentication == null || this.contextFactory == null || this.filter == null) {
            throw new IllegalStateException("The list of servers, authentication, contextFactory and securityProtocol must be set for "
                + this.getClass().getName());
        }

        this.initHandler();
    }

    protected void initHandler() throws Exception {
    	// TODO: initialize handler
    }

    protected DirContext getContext(String userName, String password, String url) throws NamingException {
        final Hashtable hashtable = new Hashtable(5, 0.75f);
        DirContext context = null;

        hashtable.put(Context.INITIAL_CONTEXT_FACTORY, this.contextFactory);
        hashtable.put(Context.PROVIDER_URL, url);
        hashtable.put(Context.SECURITY_AUTHENTICATION, this.authentication);
        hashtable.put(Context.SECURITY_PRINCIPAL, userName);
        hashtable.put(Context.SECURITY_CREDENTIALS, password);

        if (this.secure)
            hashtable.put(Context.SECURITY_PROTOCOL, this.securityProtocol);
        try {
            context = new InitialDirContext(hashtable);
            return context;
        }
        catch (AuthenticationException ae) {
            if (context != null)
                context.close();
        }

        return null;
    }

    /**
     * @return Returns the filter.
     */
    public String getFilter() {
        return this.filter;
    }

    /**
     * @param filter The filter to set.
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * @return Returns the servers.
     */
    public List getServers() {
        return this.servers;
    }

    /**
     * @param servers The servers to set.
     */
    public void setServers(List servers) {
        this.servers = servers;
    }

    /**
     * @param contextFactory The contextFactory to set.
     */
    public void setContextFactory(String contextFactory) {
        this.contextFactory = contextFactory;
    }

    /**
     * @param secure The secure to set.
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * @param securityProtocol The securityProtocol to set.
     */
    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }
}
