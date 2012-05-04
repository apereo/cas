/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

/**
 * Handles both remember me services and username and password.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.2.1
 *
 */
public class RememberMeUsernamePasswordCredentials extends
    UsernamePasswordCredentials implements RememberMeCredentials {
    
    /** Unique Id for serialization. */
    private static final long serialVersionUID = -9178853167397038282L;
    
    private boolean rememberMe;

    public final boolean isRememberMe() {
        return this.rememberMe;
    }
    
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (this.rememberMe ? 1231 : 1237);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        final RememberMeUsernamePasswordCredentials other = (RememberMeUsernamePasswordCredentials) obj;
        if (this.rememberMe != other.rememberMe)
            return false;
        return true;
    }

    public final void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
