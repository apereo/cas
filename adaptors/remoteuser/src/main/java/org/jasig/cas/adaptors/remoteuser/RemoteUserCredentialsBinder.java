/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.remoteuser;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.PrincipalBearingCredentials;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.web.bind.CredentialsBinder;


/**
 * Extracts the remote user from the request into the 
 * PrincipalBearingCredentials.  If the request has a non-null remote user,
 * this binder will place a non-null Principal into the PrincipalBearingCredentials
 * conveying that user's identity.  However, if the request has a null remote user,
 * this binder won't do anything (will leave the Principal property of the 
 * PrincipalBearingCredentials alone, which unless something else has set the Principal
 * leaves it <code>null</code>.
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
public final class RemoteUserCredentialsBinder 
    implements CredentialsBinder {

    protected final Log log = LogFactory.getLog(getClass());
    
    public void bind(HttpServletRequest request, Credentials credentials) {
        String remoteUser = request.getRemoteUser();
        
        if (remoteUser != null) {
            SimplePrincipal principal = new SimplePrincipal(remoteUser);
            ((PrincipalBearingCredentials) credentials).setPrincipal(principal);
        }
        
        if (log.isTraceEnabled()) {
            log.trace("read remote user [" + remoteUser + "], bound credentials as: [" + credentials + "]");
        }
    }

    public boolean supports(Class clazz) {
        return PrincipalBearingCredentials.class.equals(clazz);
    }


}