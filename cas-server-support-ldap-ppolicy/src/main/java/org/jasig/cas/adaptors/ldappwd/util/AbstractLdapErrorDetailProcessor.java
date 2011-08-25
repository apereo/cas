package org.jasig.cas.adaptors.ldappwd.util;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class created to manage an extensible processing chain of LDAP error
 * codes
 * 
 * @author philippe
 */
public abstract class AbstractLdapErrorDetailProcessor {

    /**
     * Logger item
     */
    protected Logger logger   = LoggerFactory.getLogger(AbstractLdapErrorDetailProcessor.class);

    /**
     * Link to next error processor class
     */
    private AbstractLdapErrorDetailProcessor nextItem = null;

    public void setNextItem(AbstractLdapErrorDetailProcessor in_item) {

        nextItem = in_item;
    }

    /**
     * error code real processing method
     * 
     * @param in_detail
     * @return true if processing was successful
     * @throws exception
     *             if processing is successful, method must throw an exception
     *             from org.jasig.cas.adaptors.ldappwd.util.exceptions package
     */
    abstract boolean processErrorDetailInternal(final String in_detail) throws AuthenticationException;

    /**
     * Entry point of a processing chain, called by class
     * BindLdapAuthenticationHandler
     * 
     * @param in_detail
     *            details of LDAP error
     * @return true if string has been found
     * @throws Exception
     *             if a matches has occured, an exception is thrown
     */
    public boolean processErrorDetail(final String in_detail) throws AuthenticationException {

        if (logger.isDebugEnabled()) {
            logger.debug("Class " + getClass() + " Processing error detail : " + in_detail);
        }
        if (processErrorDetailInternal(in_detail)) {
            return true;
        } else {
            if (nextItem != null) {
                return nextItem.processErrorDetail(in_detail);
            }
        }
        return false;
    }

    /**
     * Processing of the exception error code
     * 
     * @param in_code
     *            code généré par l'exception lancée par la méthode
     *            internalProcessErrorDetail
     * @return Chaine de webflow pour Spring
     */
    abstract String processTicketExceptionCodeInternal(final String in_code);

    /**
     * Méthode d'entrée de la chaîne de traitement qui aura lieu depuis la
     * classe LdapPwdAuthenticationViaFormAction
     * 
     * @throws Exception
     */
    public String processTicketExceptionCode(final String in_code) {

        if (logger.isDebugEnabled()) {
            logger.debug("Class " + getClass() + " Processing code : " + in_code);
        }

        String lc_returnString = processTicketExceptionCodeInternal(in_code);

        if (lc_returnString == null && nextItem != null) {
            return nextItem.processTicketExceptionCode(in_code);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Returning " + (lc_returnString == null ? "null" : lc_returnString) );
        }

        return lc_returnString;
    }

}
