package org.jasig.cas.client.receipt;

import java.io.Serializable;

import org.jasig.cas.authentication.principal.Principal;

/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface CasReceipt extends Serializable {
    public Principal getPrincipal();
    
    String getErrorCode();
    
    String getErrorMessage();
    
    String getPgtIou();
    
}
