package org.apereo.cas.authentication;

/**
 * Credential that wish to handle remember me scenarios need
 * to implement this class.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 *
 */
public interface RememberMeCredential extends Credential {

    /** Authentication attribute name for remember-me. **/
    String AUTHENTICATION_ATTRIBUTE_REMEMBER_ME = "org.apereo.cas.authentication.principal.REMEMBER_ME";

    /** Request parameter name. **/
    String REQUEST_PARAMETER_REMEMBER_ME = "rememberMe";

    /**
     * Checks if remember-me is enabled.
     *
     * @return true, if  remember me
     */
    boolean isRememberMe();

    /**
     * Sets the remember me.
     *
     * @param rememberMe the new remember me
     */
    void setRememberMe(boolean rememberMe);
}
