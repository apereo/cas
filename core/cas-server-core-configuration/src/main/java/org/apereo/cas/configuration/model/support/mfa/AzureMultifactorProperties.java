package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;

/**
 * This is {@link AzureMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-azure")
public class AzureMultifactorProperties extends BaseMultifactorProviderProperties {

    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-azure";

    private static final long serialVersionUID = 6726032660671158922L;
    
    /**
     * The authentication modes supported by Azure.
     */
    public enum AuthenticationModes {
        /**
         * Ask the user to only press the pound sign.
         */
        POUND,
        /**
         * Ask the user to enter pin code shown on the screen.
         */
        PIN
    }

    /**
     * The functionality of Azure depends on the availability of a phone number that is
     * resolved as a pre-defined attribute for the CAS principal. This is where
     * you define that attribute.
     */
    @RequiredProperty
    private String phoneAttributeName = "phone";
    /**
     * Your Microsoft Azure subscription will provide you with a license and a client certificate.
     * The client certificate is a unique private certificate that was generated especially for you.
     * These configuration files are to be placed in a directory whose path is then taught to CAS here.
     */
    @RequiredProperty
    private String configDir;
    /**
     * Password to the private key provided to you by Microsoft.
     */
    @RequiredProperty
    private String privateKeyPassword;
    /**
     * Available authentication modes supported by CAS and Azure.
     */
    private AuthenticationModes mode = AuthenticationModes.POUND;
    /**
     * Whether Azure should be allowed to make international calls.
     */
    private boolean allowInternationalCalls;

    public AzureMultifactorProperties() {
        setId(DEFAULT_IDENTIFIER);
    }

    public String getPhoneAttributeName() {
        return phoneAttributeName;
    }

    public void setPhoneAttributeName(final String phoneAttributeName) {
        this.phoneAttributeName = phoneAttributeName;
    }

    public AuthenticationModes getMode() {
        return mode;
    }

    public void setMode(final AuthenticationModes mode) {
        this.mode = mode;
    }

    public boolean isAllowInternationalCalls() {
        return allowInternationalCalls;
    }

    public void setAllowInternationalCalls(final boolean allowInternationalCalls) {
        this.allowInternationalCalls = allowInternationalCalls;
    }

    public String getConfigDir() {
        return configDir;
    }

    public void setConfigDir(final String configDir) {
        this.configDir = configDir;
    }

    public String getPrivateKeyPassword() {
        return privateKeyPassword;
    }

    public void setPrivateKeyPassword(final String privateKeyPassword) {
        this.privateKeyPassword = privateKeyPassword;
    }
}

