package org.jasig.cas.client.receipt;

import org.jasig.cas.authentication.principal.Principal;

/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class ErrorCasReceipt implements CasReceipt {

    /**
     * @see org.jasig.cas.client.receipt.AbstractCasReceipt#getErrorCode()
     */
    public String getErrorCode() {
        return "ERROR_CLIENT";
    }
    /**
     * @see org.jasig.cas.client.receipt.AbstractCasReceipt#getErrorMessage()
     */
    public String getErrorMessage() {
        return "Unable to retrieve principal information.";
    }
    
    /**
     * @see org.jasig.cas.client.receipt.CasReceipt#getPgtIou()
     */
    public String getPgtIou() {
        return null;
    }
    /**
     * @see org.jasig.cas.client.receipt.CasReceipt#getPrincipal()
     */
    public Principal getPrincipal() {
        return null;
    }
}
