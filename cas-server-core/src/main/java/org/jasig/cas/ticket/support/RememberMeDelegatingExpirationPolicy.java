/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.support;

import org.inspektr.common.ioc.annotation.NotNull;
import org.jasig.cas.authentication.principal.RememberMeCredentials;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketState;

/**
 * Delegates to different expiration policies depending on whether remember me
 * is true or not.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.2.1
 *
 */
public final class RememberMeDelegatingExpirationPolicy implements ExpirationPolicy {
    
    /** Unique Id for Serialization */
    private static final long serialVersionUID = -575145836880428365L;

    @NotNull
    private ExpirationPolicy rememberMeExpirationPolicy;
    
    @NotNull
    private ExpirationPolicy sessionExpirationPolicy;

    public boolean isExpired(TicketState ticketState) {
        final Boolean b = (Boolean) ticketState.getAuthentication().getAttributes().get(RememberMeCredentials.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);
        
        if (b == null || b.equals(Boolean.FALSE)) {
            return this.sessionExpirationPolicy.isExpired(ticketState);
        }
        
        return this.rememberMeExpirationPolicy.isExpired(ticketState);
    }
    
    public void setRememberMeExpirationPolicy(
        final ExpirationPolicy rememberMeExpirationPolicy) {
        this.rememberMeExpirationPolicy = rememberMeExpirationPolicy;
    }

    public void setSessionExpirationPolicy(final ExpirationPolicy sessionExpirationPolicy) {
        this.sessionExpirationPolicy = sessionExpirationPolicy;
    }
}
