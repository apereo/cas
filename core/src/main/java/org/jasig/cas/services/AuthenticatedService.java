/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.services;


/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class AuthenticatedService {
    private String id;

    private boolean allowedToProxy;
    
    private boolean forceAuthentication;
    
    private String theme;

    public AuthenticatedService(final String id, final boolean allowedToProxy, final boolean forceAuthentication, final String theme) {
        this.id = id;
        this.allowedToProxy = allowedToProxy;
        this.forceAuthentication = forceAuthentication;
        this.theme = theme;
    }
    
    /**
     * @return Returns the allowedToProxy.
     */
    public boolean isAllowedToProxy() {
        return this.allowedToProxy;
    }
    /**
     * @param allowedToProxy The allowedToProxy to set.
     */
    public void setAllowedToProxy(boolean allowedToProxy) {
        this.allowedToProxy = allowedToProxy;
    }
    /**
     * @return Returns the forceAuthentication.
     */
    public boolean isForceAuthentication() {
        return this.forceAuthentication;
    }
    /**
     * @param forceAuthentication The forceAuthentication to set.
     */
    public void setForceAuthentication(boolean forceAuthentication) {
        this.forceAuthentication = forceAuthentication;
    }
    /**
     * @return Returns the id.
     */
    public String getId() {
        return this.id;
    }
    /**
     * @param id The id to set.
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * @return Returns the theme.
     */
    public String getTheme() {
        return this.theme;
    }
    /**
     * @param theme The theme to set.
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }
}
