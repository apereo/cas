package org.jasig.cas.client.receipt;

import org.jasig.cas.authentication.principal.Principal;

/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class CasResponseCasReceipt implements CasReceipt {
    private Principal principal = null;
    private String errorCode;
    private String errorMessage;
    private String pgtIou;
   
    /**
     * @return Returns the errorCode.
     */
    public String getErrorCode() {
        return errorCode;
    }
    /**
     * @param errorCode The errorCode to set.
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    /**
     * @return Returns the errorMessage.
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    /**
     * @param errorMessage The errorMessage to set.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    /**
     * @return Returns the pgtIou.
     */
    public String getPgtIou() {
        return pgtIou;
    }
    /**
     * @param pgtIou The pgtIou to set.
     */
    public void setPgtIou(String pgtIou) {
        this.pgtIou = pgtIou;
    }
    /**
     * @return Returns the principal.
     */
    public Principal getPrincipal() {
        return principal;
    }
    /**
     * @param principal The principal to set.
     */
    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }
}
