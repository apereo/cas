/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.ldap;

import java.util.Iterator;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.util.LdapUtils;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class FastBindLdapAuthenticationHandler extends AbstractLdapAuthenticationHandler {

    /**
     * @see org.jasig.cas.authentication.handler.AuthenticationHandler#authenticate(org.jasig.cas.authentication.AuthenticationRequest)
     */
    public boolean authenticate(final Credentials request) {
        final UsernamePasswordCredentials uRequest = (UsernamePasswordCredentials)request;

        for (Iterator iter = this.getServers().iterator(); iter.hasNext();) {
            DirContext dirContext = null;
            final String url = (String)iter.next();

            try {
                dirContext = this.getContext(LdapUtils.getFilterWithValues(this.getFilter(), uRequest.getUserName()), uRequest.getPassword(), url);

                if (dirContext == null)
                    return false;

                dirContext.close();
                return true;
            }
            catch (NamingException e) {
                log.debug("LDAP ERROR: Unable to connect to LDAP server " + url + ".  Attempting to contact next server (if exists).");
            }
        }

        return false;
    }
}
