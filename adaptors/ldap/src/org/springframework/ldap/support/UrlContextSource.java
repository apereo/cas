/*
 * Copyright 2002-2004 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package org.springframework.ldap.support;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Class providing a context using the given protocol, authentication, principal and credentials for the given url.
 * 
 * @author Olivier Jolly
 */
public class UrlContextSource implements ContextSource {

    private String ldapUrl = null;

    private Hashtable environment = new Hashtable();

    /**
     * @return Returns the ldapUrl.
     */
    public String getLdapUrl() {
        return this.ldapUrl;
    }

    /**
     * @param ldapUrl The ldapUrl to set.
     */
    public void setLdapUrl(String ldapUrl) {
        this.ldapUrl = ldapUrl;
    }

    /**
     * @return Returns the securityAuthentication.
     */
    public String getSecurityAuthentication() {
        return (String)this.environment.get(Context.SECURITY_AUTHENTICATION);
    }

    /**
     * @param securityAuthentication The securityAuthentication to set.
     */
    public void setSecurityAuthentication(String securityAuthentication) {
        this.environment.put(Context.SECURITY_AUTHENTICATION, securityAuthentication);
    }

    /**
     * @return Returns the securityCredentials.
     */
    public String getSecurityCredentials() {
        return (String)this.environment.get(Context.SECURITY_CREDENTIALS);
    }

    /**
     * @param securityCredentials The securityCredentials to set.
     */
    public void setSecurityCredentials(String securityCredentials) {
        this.environment.put(Context.SECURITY_CREDENTIALS, securityCredentials);
    }

    /**
     * @return Returns the securityPrincipal.
     */
    public String getSecurityPrincipal() {
        return (String)this.environment.get(Context.SECURITY_PRINCIPAL);
    }

    /**
     * @param securityPrincipal The securityPrincipal to set.
     */
    public void setSecurityPrincipal(String securityPrincipal) {
        this.environment.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
    }

    /**
     * @return Returns the securityProtocol.
     */
    public String getSecurityProtocol() {
        return (String)this.environment.get(Context.SECURITY_PROTOCOL);
    }

    /**
     * @param securityProtocol The securityProtocol to set.
     */
    public void setSecurityProtocol(String securityProtocol) {
        this.environment.put(Context.SECURITY_PROTOCOL, securityProtocol);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.ContextSourceInterface#getDirContext()
     */
    public DirContext getDirContext() {
        try {
            // TODO check whether there is a faster way to retrieve context,
            // like lookup("") on a lazy initiated initial context
            return (DirContext)new InitialDirContext(this.environment).lookup(this.ldapUrl);
        }
        catch (NamingException ex) {
            throw new DataAccessResourceFailureException("Cannot retrieve context", ex);
        }
    }

    public DirContext getDirContext(final String principal, final String password) {
        Hashtable environment = (Hashtable)this.environment.clone();

        environment.put(Context.SECURITY_PRINCIPAL, principal);
        environment.put(Context.SECURITY_CREDENTIALS, password);

        try {
            // TODO check whether there is a faster way to retrieve context,
            // like lookup("") on a lazy initiated initial context
            return (DirContext)new InitialDirContext(environment).lookup(this.ldapUrl);
        }
        catch (NamingException ex) {
            throw new DataAccessResourceFailureException("Cannot retrieve context", ex);
        }
    }

}